package com.heartrisk.data;

import com.heartrisk.model.Patient;
import com.heartrisk.model.RiskAssessment;
import java.util.List;
import java.util.Optional;

/**
 * Storage abstraction for patients and their risk assessments.
 *
 * <p><b>DESIGN PATTERN: Repository.</b> Services depend on this interface only, so the storage
 * technology (CSV files today, a database tomorrow) can change without touching them.
 * Identifiers are allocated by {@code nextPatientId()} / {@code nextAssessmentId()}, which
 * return the next free id without reserving it; callers serialise creation (the services do).</p>
 */
public interface PatientRepository {

    /**
     * Stores a new patient.
     *
     * @param patient patient to store
     * @throws IllegalArgumentException if a patient with the same id already exists
     */
    void save(Patient patient);

    /**
     * Finds a patient.
     *
     * @param id patient identifier
     * @return the patient, or empty if unknown
     */
    Optional<Patient> findById(String id);

    /** @return all stored patients in insertion order */
    List<Patient> findAll();

    /**
     * Stores a new assessment.
     *
     * @param assessment assessment to store
     */
    void saveAssessment(RiskAssessment assessment);

    /**
     * Finds one assessment.
     *
     * @param id assessment identifier
     * @return the assessment, or empty if unknown
     */
    Optional<RiskAssessment> findAssessmentById(String id);

    /**
     * Lists the assessments of one patient, oldest first.
     *
     * @param patientId patient identifier
     * @return assessments (possibly empty)
     */
    List<RiskAssessment> findAssessmentsByPatient(String patientId);

    /** @return next free patient id, for example {@code P-0001} */
    String nextPatientId();

    /** @return next free assessment id, for example {@code A-0001} */
    String nextAssessmentId();
}
