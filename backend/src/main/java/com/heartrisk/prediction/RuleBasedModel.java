package com.heartrisk.prediction;

import com.heartrisk.model.HealthRecord;

/**
 * Transparent, points-based risk score. Each risk factor that is present adds points; the total
 * is divided by the maximum possible points, giving a probability-like value from 0 to 1.
 *
 * <p>Points: age &gt; 55 (1), cholesterol &gt; 240 (1), resting blood pressure &gt; 140 (1),
 * exercise-induced angina (2), ST depression &gt; 2 (2), major vessels &gt; 0 (2) and maximum heart
 * rate &lt; 120 (1) - ten points in total. Implements {@link PredictionModel} (a Strategy).</p>
 */
public class RuleBasedModel implements PredictionModel {

    private static final String NAME = "Rule-Based Score";
    private static final int MAX_POINTS = 10;

    @Override
    public double predictProbability(HealthRecord record) {
        int points = 0;
        if (record.getAge() > 55) {
            points += 1;
        }
        if (record.getChol() > 240) {
            points += 1;
        }
        if (record.getTrestbps() > 140) {
            points += 1;
        }
        if (record.getExang() == 1) {
            points += 2;
        }
        if (record.getOldpeak() > 2.0) {
            points += 2;
        }
        if (record.getCa() > 0) {
            points += 2;
        }
        if (record.getThalach() < 120) {
            points += 1;
        }
        return (double) points / MAX_POINTS;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
