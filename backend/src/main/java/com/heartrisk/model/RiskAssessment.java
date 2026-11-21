package com.heartrisk.model;

import java.time.Instant;

/**
 * Immutable result of one prediction: which patient, which model, how likely, and when.
 * The {@link RiskLevel} is always derived from the probability, so the two never disagree.
 */
public final class RiskAssessment {

    private final String id;
    private final String patientId;
    private final double probability;
    private final RiskLevel level;
    private final String modelName;
    private final Instant timestamp;

    /**
     * Creates an assessment.
     *
     * @param id          assessment identifier
     * @param patientId   identifier of the assessed patient
     * @param probability predicted probability of heart disease, 0..1
     * @param modelName   name of the model that produced the value
     * @param timestamp   creation time
     * @throws IllegalArgumentException if an identifier or the model name is blank or the
     *                                  probability is outside [0, 1]
     */
    public RiskAssessment(String id, String patientId, double probability,
                          String modelName, Instant timestamp) {
        if (id == null || id.isBlank() || patientId == null || patientId.isBlank()
                || modelName == null || modelName.isBlank() || timestamp == null) {
            throw new IllegalArgumentException("id, patientId, modelName and timestamp are required");
        }
        this.id = id;
        this.patientId = patientId;
        this.probability = probability;
        this.level = RiskLevel.fromProbability(probability);
        this.modelName = modelName;
        this.timestamp = timestamp;
    }

    /** @return assessment identifier */
    public String getId() {
        return id;
    }

    /** @return identifier of the assessed patient */
    public String getPatientId() {
        return patientId;
    }

    /** @return predicted probability, 0..1 */
    public double getProbability() {
        return probability;
    }

    /** @return risk level derived from the probability */
    public RiskLevel getLevel() {
        return level;
    }

    /** @return name of the model that produced the probability */
    public String getModelName() {
        return modelName;
    }

    /** @return creation time */
    public Instant getTimestamp() {
        return timestamp;
    }
}
