package com.heartrisk.model;

/**
 * Categorical risk level derived from a predicted probability.
 *
 * <p>Thresholds: probability below 0.33 is {@link #LOW}, below 0.66 is {@link #MEDIUM},
 * otherwise {@link #HIGH}.</p>
 */
public enum RiskLevel {
    /** Probability below 0.33. */
    LOW,
    /** Probability from 0.33 up to (not including) 0.66. */
    MEDIUM,
    /** Probability of 0.66 or more. */
    HIGH;

    private static final double LOW_LIMIT = 0.33;
    private static final double MEDIUM_LIMIT = 0.66;

    /**
     * Maps a probability to its level.
     *
     * @param probability value in the closed interval [0, 1]
     * @return the matching level
     * @throws IllegalArgumentException if the value is NaN or outside [0, 1]
     */
    public static RiskLevel fromProbability(double probability) {
        if (Double.isNaN(probability) || probability < 0.0 || probability > 1.0) {
            throw new IllegalArgumentException("probability must be between 0 and 1");
        }
        if (probability < LOW_LIMIT) {
            return LOW;
        }
        return probability < MEDIUM_LIMIT ? MEDIUM : HIGH;
    }
}
