package com.heartrisk.prediction;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.heartrisk.TestRecords;
import org.junit.jupiter.api.Test;

/** Points scoring of {@link RuleBasedModel} and the metrics in {@link EvaluationResult}. */
class RuleBasedModelTest {

    private final RuleBasedModel model = new RuleBasedModel();

    @Test
    void noRiskFactorsGivesZero() {
        assertEquals(0.0, model.predictProbability(TestRecords.lowRiskBuilder().build()), 1e-9);
    }

    @Test
    void allRiskFactorsGiveOne() {
        assertEquals(1.0, model.predictProbability(TestRecords.highRiskBuilder().build()), 1e-9);
    }

    @Test
    void eachRuleAddsItsOwnPoints() {
        assertEquals(0.1, model.predictProbability(TestRecords.lowRiskBuilder().age(56).build()), 1e-9);
        assertEquals(0.2, model.predictProbability(TestRecords.lowRiskBuilder().exang(1).build()), 1e-9);
        assertEquals(0.2, model.predictProbability(TestRecords.lowRiskBuilder().oldpeak(2.5).build()), 1e-9);
        assertEquals(0.2, model.predictProbability(TestRecords.lowRiskBuilder().ca(1).build()), 1e-9);
        assertEquals(0.1, model.predictProbability(TestRecords.lowRiskBuilder().thalach(119).build()), 1e-9);
    }

    @Test
    void thresholdsAreStrict() {
        assertEquals(0.0, model.predictProbability(
                TestRecords.lowRiskBuilder().age(55).chol(240).trestbps(140).oldpeak(2.0).thalach(120).build()), 1e-9);
    }

    @Test
    void evaluationMetricsAreComputedFromTheConfusionMatrix() {
        EvaluationResult r = new EvaluationResult(40, 10, 40, 10);
        assertEquals(0.8, r.accuracy(), 1e-9);
        assertEquals(0.8, r.precision(), 1e-9);
        assertEquals(0.8, r.recall(), 1e-9);
        assertEquals(0.8, r.f1(), 1e-9);
        assertEquals(0.0, new EvaluationResult(0, 0, 0, 0).f1(), 1e-9);
    }
}
