package com.heartrisk.prediction;

import com.heartrisk.model.HealthRecord;

/**
 * Common contract of every risk model.
 *
 * <p><b>OOAD: Interface and polymorphism.</b> {@link LogisticRegressionModel} and
 * {@link RuleBasedModel} implement this interface, so callers such as
 * {@code PredictionService} work with any model without knowing its concrete class.
 * <b>DESIGN PATTERN: Strategy</b> - each implementation is one interchangeable algorithm.</p>
 */
public interface PredictionModel {

    /**
     * Estimates the probability of heart disease.
     *
     * @param record the patient's health record
     * @return probability between 0.0 and 1.0
     */
    double predictProbability(HealthRecord record);

    /**
     * Human-readable model name stored in every assessment.
     *
     * @return model name
     */
    String getName();
}
