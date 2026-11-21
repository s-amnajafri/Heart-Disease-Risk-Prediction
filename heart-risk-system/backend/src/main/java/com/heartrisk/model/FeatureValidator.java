package com.heartrisk.model;

/** Range and type checks shared by {@link HealthRecord.Builder}. */
final class FeatureValidator {

    private FeatureValidator() {
    }

    /**
     * Converts a value to int, rejecting fractions.
     *
     * @param name  feature name for the error message
     * @param value numeric value
     * @return the value as int
     */
    static int whole(String name, double value) {
        if (Double.isNaN(value) || value != Math.rint(value)) {
            throw new IllegalArgumentException(name + " must be a whole number");
        }
        return (int) value;
    }

    /**
     * Checks that a feature is present and within [min, max].
     *
     * @param name  feature name for the error message
     * @param value value, or {@code null} if it was never set
     * @param min   smallest allowed value
     * @param max   largest allowed value
     * @throws IllegalArgumentException if the value is missing, NaN or outside the range
     */
    static void checkRange(String name, Number value, double min, double max) {
        if (value == null) {
            throw new IllegalArgumentException(name + " is required");
        }
        double v = value.doubleValue();
        if (Double.isNaN(v) || v < min || v > max) {
            throw new IllegalArgumentException(
                    name + " must be between " + show(min) + " and " + show(max) + " but was " + show(v));
        }
    }

    private static String show(double v) {
        return v == Math.rint(v) ? String.valueOf((long) v) : String.valueOf(v);
    }
}
