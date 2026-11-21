package com.heartrisk.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A doctor who reviews patients.
 *
 * <p><b>OOAD: Aggregation.</b> A doctor <i>holds</i> a {@code List<Patient>} but does not own the
 * patients: they are created elsewhere, exist independently, and stay alive when removed from
 * the doctor's list or when the doctor object is discarded.</p>
 */
public class Doctor extends User {

    private final String specialty;
    private final List<Patient> patients = new CopyOnWriteArrayList<>();

    /**
     * Creates a doctor.
     *
     * @param id        doctor identifier
     * @param name      doctor name
     * @param email     doctor e-mail
     * @param specialty medical specialty, for example {@code "Cardiology"}
     */
    public Doctor(String id, String name, String email, String specialty) {
        super(id, name, email);
        if (specialty == null || specialty.isBlank()) {
            throw new IllegalArgumentException("specialty must not be blank");
        }
        this.specialty = specialty.trim();
    }

    /** @return medical specialty */
    public String getSpecialty() {
        return specialty;
    }

    /**
     * Adds a patient to this doctor's list; a patient already present (same id) is ignored.
     *
     * @param patient the existing patient to refer to
     */
    public void addPatient(Patient patient) {
        Objects.requireNonNull(patient, "patient must not be null");
        boolean known = patients.stream().anyMatch(p -> p.getId().equals(patient.getId()));
        if (!known) {
            patients.add(patient);
        }
    }

    /**
     * Removes the patient from this doctor's list only; the patient object is not destroyed.
     *
     * @param patient the patient to remove
     * @return {@code true} if the patient was in the list
     */
    public boolean removePatient(Patient patient) {
        return patients.remove(patient);
    }

    /** @return read-only view of the patients this doctor refers to */
    public List<Patient> getPatients() {
        return Collections.unmodifiableList(patients);
    }

    @Override
    public String getRole() {
        return "DOCTOR";
    }
}
