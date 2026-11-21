package com.heartrisk.prediction;

/**
 * Confusion-matrix counts for the positive class "heart disease present" and the metrics
 * derived from them. Ratios with a zero denominator are reported as 0.0.
 *
 * @param truePositives  disease present, predicted present
 * @param falsePositives disease absent, predicted present
 * @param trueNegatives  disease absent, predicted absent
 * @param falseNegatives disease present, predicted absent
 */
public record EvaluationResult(int truePositives, int falsePositives,
                               int trueNegatives, int falseNegatives) {

    /** @return number of evaluated samples */
    public int total() {
        return truePositives + falsePositives + trueNegatives + falseNegatives;
    }

    /** @return share of correct predictions */
    public double accuracy() {
        return ratio(truePositives + trueNegatives, total());
    }

    /** @return share of predicted positives that are truly positive */
    public double precision() {
        return ratio(truePositives, truePositives + falsePositives);
    }

    /** @return share of true positives that were found */
    public double recall() {
        return ratio(truePositives, truePositives + falseNegatives);
    }

    /** @return harmonic mean of precision and recall */
    public double f1() {
        double p = precision();
        double r = recall();
        return ratio(2 * p * r, p + r);
    }

    private static double ratio(double numerator, double denominator) {
        return denominator == 0 ? 0.0 : numerator / denominator;
    }
}
