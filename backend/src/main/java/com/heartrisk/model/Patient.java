package com.heartrisk.model;

import java.util.Objects;

/**
 * A person whose heart-disease risk is assessed.
 *
 * <p><b>OOAD: Composition.</b> A patient <i>owns</i> exactly one {@link HealthRecord}. The record
 * is created inside the constructor from a {@link HealthRecord.Builder}, so no outside object
 * holds a reference that could share it, and it lives and dies with the patient.</p>
 */
public class Patient extends User {

    private final HealthRecord healthRecord;
    private final String doctorId;

    /**
     * Creates a patient together with the health record the patient owns.
     *
     * @param id            patient identifier
     * @param name          patient name
     * @param email         patient e-mail
     * @param recordBuilder builder holding the 13 clinical features; {@code build()} validates them
     * @throws IllegalArgumentException if the user data or any feature is invalid
     */
    public Patient(String id, String name, String email, HealthRecord.Builder recordBuilder) {
        this(id, name, email, recordBuilder, "D-001");
    }

    /** Creates a patient with an explicitly assigned doctor. */
    public Patient(String id, String name, String email, HealthRecord.Builder recordBuilder, String doctorId) {
        super(id, name, email);
        Objects.requireNonNull(recordBuilder, "recordBuilder must not be null");
        if (doctorId == null || doctorId.isBlank()) {
            throw new IllegalArgumentException("doctorId must not be blank");
        }
        this.healthRecord = recordBuilder.build();
        this.doctorId = doctorId.trim();
    }

    /** @return identifier of the doctor assigned to this patient */
    public String getDoctorId() { return doctorId; }

    /** @return the (immutable) health record owned by this patient */
    public HealthRecord getHealthRecord() {
        return healthRecord;
    }

    @Override
    public String getRole() {
        return "PATIENT";
    }
}
