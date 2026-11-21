package com.heartrisk.data;

import com.heartrisk.model.HealthRecord;
import com.heartrisk.model.Patient;
import com.heartrisk.model.RiskAssessment;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * {@link PatientRepository} that keeps everything in memory and appends every change to
 * {@code patients.csv} and {@code assessments.csv} in the data directory, so data survives a
 * restart. Existing files are read when the repository is created. All methods are synchronised
 * because the REST server handles requests on several threads.
 *
 * <p><b>DESIGN PATTERN: Repository</b> (concrete implementation).</p>
 */
public class CsvPatientRepository implements PatientRepository {

    private static final String ASSESSMENT_HEADER = "id,patientId,probability,level,modelName,timestamp";
    private static final int PATIENT_FIXED_COLUMNS = 3;
    private static final String LEGACY_DOCTOR_ID = "D-001";

    private final Path patientsFile;
    private final Path assessmentsFile;
    private final Map<String, Patient> patients = new LinkedHashMap<>();
    private final List<RiskAssessment> assessments = new ArrayList<>();
    private int patientCounter;
    private int assessmentCounter;

    /**
     * Opens (and creates, if needed) the two CSV files in the given directory.
     *
     * @param dataDirectory directory for patients.csv and assessments.csv
     * @throws IOException if the files cannot be created or read
     */
    public CsvPatientRepository(Path dataDirectory) throws IOException {
        Files.createDirectories(dataDirectory);
        this.patientsFile = dataDirectory.resolve("patients.csv");
        this.assessmentsFile = dataDirectory.resolve("assessments.csv");
        ensureFile(patientsFile, CsvUtil.join(patientHeader()));
        migratePatientFileIfNeeded();
        ensureFile(assessmentsFile, ASSESSMENT_HEADER);
        loadPatients();
        loadAssessments();
    }

    @Override
    public synchronized void save(Patient patient) {
        if (patients.containsKey(patient.getId())) {
            throw new IllegalArgumentException("Patient already exists: " + patient.getId());
        }
        List<Object> row = new ArrayList<>(List.of(patient.getId(), patient.getName(), patient.getEmail()));
        row.addAll(patient.getHealthRecord().toMap().values());
        row.add(patient.getDoctorId());
        appendLine(patientsFile, CsvUtil.join(row));
        patients.put(patient.getId(), patient);
        patientCounter = Math.max(patientCounter, numericSuffix(patient.getId()));
    }

    @Override
    public synchronized Optional<Patient> findById(String id) {
        return Optional.ofNullable(patients.get(id));
    }

    @Override
    public synchronized List<Patient> findAll() {
        return List.copyOf(patients.values());
    }

    @Override
    public synchronized void saveAssessment(RiskAssessment a) {
        appendLine(assessmentsFile, CsvUtil.join(List.of(a.getId(), a.getPatientId(),
                a.getProbability(), a.getLevel(), a.getModelName(), a.getTimestamp())));
        assessments.add(a);
        assessmentCounter = Math.max(assessmentCounter, numericSuffix(a.getId()));
    }

    @Override
    public synchronized Optional<RiskAssessment> findAssessmentById(String id) {
        return assessments.stream().filter(a -> a.getId().equals(id)).findFirst();
    }

    @Override
    public synchronized List<RiskAssessment> findAssessmentsByPatient(String patientId) {
        return assessments.stream().filter(a -> a.getPatientId().equals(patientId)).toList();
    }

    @Override
    public synchronized String nextPatientId() {
        return String.format("P-%04d", patientCounter + 1);
    }

    @Override
    public synchronized String nextAssessmentId() {
        return String.format("A-%04d", assessmentCounter + 1);
    }

    private static List<String> patientHeader() {
        List<String> header = new ArrayList<>(List.of("id", "name", "email"));
        header.addAll(HealthRecord.FEATURE_NAMES);
        header.add("doctorId");
        return header;
    }


    /** Adds the doctorId column to patient files created by older versions. */
    private void migratePatientFileIfNeeded() throws IOException {
        List<String> lines = Files.readAllLines(patientsFile, StandardCharsets.UTF_8);
        if (lines.isEmpty() || lines.get(0).endsWith(",doctorId")) return;
        List<String> migrated = new ArrayList<>();
        migrated.add(CsvUtil.join(patientHeader()));
        for (int i = 1; i < lines.size(); i++) {
            if (!lines.get(i).isBlank()) migrated.add(lines.get(i) + "," + LEGACY_DOCTOR_ID);
        }
        Files.write(patientsFile, migrated, StandardCharsets.UTF_8);
    }

    private void loadPatients() throws IOException {
        for (List<String> cells : readRows(patientsFile)) {
            HealthRecord.Builder builder = HealthRecord.builder();
            for (int i = 0; i < HealthRecord.FEATURE_NAMES.size(); i++) {
                builder.feature(HealthRecord.FEATURE_NAMES.get(i),
                        Double.parseDouble(cells.get(PATIENT_FIXED_COLUMNS + i)));
            }
            String doctorId = cells.size() > PATIENT_FIXED_COLUMNS + HealthRecord.FEATURE_NAMES.size()
                    ? cells.get(PATIENT_FIXED_COLUMNS + HealthRecord.FEATURE_NAMES.size()) : LEGACY_DOCTOR_ID;
            Patient patient = new Patient(cells.get(0), cells.get(1), cells.get(2), builder, doctorId);
            patients.put(patient.getId(), patient);
            patientCounter = Math.max(patientCounter, numericSuffix(patient.getId()));
        }
    }

    /** Column 3 (level) is redundant: the level is recomputed from the probability. */
    private void loadAssessments() throws IOException {
        for (List<String> c : readRows(assessmentsFile)) {
            RiskAssessment a = new RiskAssessment(c.get(0), c.get(1),
                    Double.parseDouble(c.get(2)), c.get(4), Instant.parse(c.get(5)));
            assessments.add(a);
            assessmentCounter = Math.max(assessmentCounter, numericSuffix(a.getId()));
        }
    }

    private static List<List<String>> readRows(Path file) throws IOException {
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        List<List<String>> rows = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            if (!lines.get(i).isBlank()) {
                rows.add(CsvUtil.split(lines.get(i)));
            }
        }
        return rows;
    }

    private static void ensureFile(Path file, String header) throws IOException {
        if (!Files.exists(file)) {
            Files.writeString(file, header + "\n", StandardCharsets.UTF_8);
        }
    }

    private static void appendLine(Path file, String line) {
        try {
            Files.writeString(file, line + "\n", StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not write " + file, e);
        }
    }

    private static int numericSuffix(String id) {
        int dash = id.indexOf('-');
        try {
            return Integer.parseInt(id.substring(dash + 1));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
