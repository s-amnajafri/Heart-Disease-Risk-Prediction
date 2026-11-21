package com.heartrisk.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.heartrisk.TestRecords;
import com.heartrisk.model.Patient;
import com.heartrisk.model.RiskAssessment;
import com.heartrisk.model.RiskLevel;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** The Repository pattern: {@link CsvPatientRepository} persists to CSV and reloads. */
class CsvPatientRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    void savedDataSurvivesReopeningTheRepository() throws IOException {
        CsvPatientRepository repo = new CsvPatientRepository(tempDir);
        assertEquals("P-0001", repo.nextPatientId());
        Patient saved = new Patient(repo.nextPatientId(), "Lee, Ann \"AJ\"", "ann@example.com",
                TestRecords.validBuilder().oldpeak(2.5));
        repo.save(saved);
        repo.saveAssessment(new RiskAssessment("A-0001", "P-0001", 0.72, "Rule-Based Score", Instant.parse("2026-01-01T10:00:00Z")));

        CsvPatientRepository reopened = new CsvPatientRepository(tempDir);
        Patient loaded = reopened.findById("P-0001").orElseThrow();
        assertEquals("Lee, Ann \"AJ\"", loaded.getName());
        assertEquals(2.5, loaded.getHealthRecord().getOldpeak(), 1e-9);
        assertEquals("D-001", loaded.getDoctorId());
        RiskAssessment a = reopened.findAssessmentsByPatient("P-0001").get(0);
        assertEquals(RiskLevel.HIGH, a.getLevel());
        assertEquals(Instant.parse("2026-01-01T10:00:00Z"), a.getTimestamp());
        assertEquals("P-0002", reopened.nextPatientId());
        assertEquals("A-0002", reopened.nextAssessmentId());
    }


    @Test
    void assignedDoctorSurvivesReopeningTheRepository() throws IOException {
        CsvPatientRepository repo = new CsvPatientRepository(tempDir);
        Patient saved = new Patient(repo.nextPatientId(), "Mina", "mina@example.com",
                TestRecords.validBuilder(), "D-003");
        repo.save(saved);

        CsvPatientRepository reopened = new CsvPatientRepository(tempDir);
        assertEquals("D-003", reopened.findById("P-0001").orElseThrow().getDoctorId());
    }

    @Test
    void oldPatientFilesAreMigratedToDefaultDoctor() throws IOException {
        String header = "id,name,email,age,sex,cp,trestbps,chol,fbs,restecg,thalach,exang,oldpeak,slope,ca,thal";
        String row = "P-0001,Ann,ann@example.com,55,1,0,130,240,0,1,150,0,1.0,1,0,2";
        Files.writeString(tempDir.resolve("patients.csv"), header + "\n" + row + "\n");

        CsvPatientRepository migrated = new CsvPatientRepository(tempDir);
        assertEquals("D-001", migrated.findById("P-0001").orElseThrow().getDoctorId());
        assertTrue(Files.readString(tempDir.resolve("patients.csv")).startsWith(header + ",doctorId"));
    }

    @Test
    void duplicatePatientIdIsRejected() throws IOException {
        CsvPatientRepository repo = new CsvPatientRepository(tempDir);
        Patient p = new Patient("P-0001", "Ann", "ann@example.com", TestRecords.validBuilder());
        repo.save(p);
        assertThrows(IllegalArgumentException.class, () -> repo.save(p));
    }

    @Test
    void lookupsOfUnknownIdsAreEmpty() throws IOException {
        CsvPatientRepository repo = new CsvPatientRepository(tempDir);
        assertTrue(repo.findById("P-9999").isEmpty());
        assertTrue(repo.findAssessmentById("A-9999").isEmpty());
        assertTrue(repo.findAssessmentsByPatient("P-9999").isEmpty());
    }

    @Test
    void filesAreCreatedWithHeaders() throws IOException {
        new CsvPatientRepository(tempDir);
        assertTrue(Files.readString(tempDir.resolve("patients.csv")).startsWith("id,name,email,age,sex"));
        assertTrue(Files.readString(tempDir.resolve("assessments.csv")).startsWith("id,patientId,probability"));
    }
}
