package com.heartrisk.service;

import com.heartrisk.data.PatientRepository;
import com.heartrisk.model.Doctor;
import com.heartrisk.model.HealthRecord;
import com.heartrisk.model.Patient;
import com.heartrisk.model.RiskAssessment;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

/** Registers patients and looks up patients and their assessments. */
public class PatientService {

    private final PatientRepository repository;
    private final Map<String, Doctor> doctors;

    /**
     * Creates the service.
     *
     * @param repository storage for patients and assessments
     * @param doctor     doctor who is assigned every newly registered patient (aggregation)
     */
    public PatientService(PatientRepository repository, Doctor doctor) {
        this(repository, List.of(doctor));
    }

    /** Creates the service with the available doctors. */
    public PatientService(PatientRepository repository, List<Doctor> doctors) {
        this.repository = repository;
        this.doctors = new LinkedHashMap<>();
        for (Doctor doctor : doctors) this.doctors.put(doctor.getId(), doctor);
        if (this.doctors.isEmpty()) throw new IllegalArgumentException("At least one doctor is required");
        repository.findAll().forEach(patient -> {
            Doctor assigned = this.doctors.get(patient.getDoctorId());
            if (assigned == null) assigned = this.doctors.values().iterator().next();
            assigned.addPatient(patient);
        });
    }

    /** @return available doctors in configured order */
    public List<Doctor> getDoctors() { return List.copyOf(doctors.values()); }

    /**
     * Creates, stores and assigns a new patient.
     *
     * @param name    patient name
     * @param email   patient e-mail
     * @param builder the patient's 13 features
     * @return the stored patient
     * @throws IllegalArgumentException if any value is invalid
     */
    public synchronized Patient register(String name, String email, HealthRecord.Builder builder, String doctorId) {
        Doctor doctor = doctors.get(doctorId);
        if (doctor == null) throw new NotFoundException("Doctor not found: " + doctorId);
        Patient patient = new Patient(repository.nextPatientId(), name, email, builder, doctorId);
        repository.save(patient);
        doctor.addPatient(patient);
        return patient;
    }

    /** Backward-compatible registration using the first configured doctor. */
    public synchronized Patient register(String name, String email, HealthRecord.Builder builder) {
        return register(name, email, builder, doctors.keySet().iterator().next());
    }

    /** @return all registered patients in insertion order */
    public List<Patient> getAllPatients() { return repository.findAll(); }

    /**
     * Finds a patient.
     *
     * @param id patient identifier
     * @return the patient
     * @throws NotFoundException if the id is unknown
     */
    public Patient getPatient(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Patient not found: " + id));
    }

    /**
     * Finds an assessment.
     *
     * @param id assessment identifier
     * @return the assessment
     * @throws NotFoundException if the id is unknown
     */
    public RiskAssessment getAssessment(String id) {
        return repository.findAssessmentById(id)
                .orElseThrow(() -> new NotFoundException("Assessment not found: " + id));
    }

    /**
     * Lists a patient's assessments, oldest first.
     *
     * @param patientId patient identifier
     * @return the assessments
     * @throws NotFoundException if the patient is unknown
     */
    public List<RiskAssessment> getHistory(String patientId) {
        getPatient(patientId);
        return repository.findAssessmentsByPatient(patientId);
    }
}
