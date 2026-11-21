package com.heartrisk.prediction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** The Factory pattern: {@link ModelFactory#create(String)}. */
class ModelFactoryTest {

    @Test
    void logisticKeyCreatesLogisticRegressionModel() {
        assertInstanceOf(LogisticRegressionModel.class, ModelFactory.create("logistic"));
    }

    @Test
    void ruleKeyCreatesRuleBasedModel() {
        assertInstanceOf(RuleBasedModel.class, ModelFactory.create("rule"));
    }

    @Test
    void keyIsCaseAndWhitespaceInsensitive() {
        assertInstanceOf(RuleBasedModel.class, ModelFactory.create("  RULE "));
        assertInstanceOf(LogisticRegressionModel.class, ModelFactory.create("Logistic"));
    }

    @Test
    void unknownOrNullKeyIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> ModelFactory.create("neural"));
        assertThrows(IllegalArgumentException.class, () -> ModelFactory.create(null));
    }

    @Test
    void everyCallReturnsANewInstance() {
        assertNotSame(ModelFactory.create("rule"), ModelFactory.create("rule"));
    }

    @Test
    void modelsReportTheirNames() {
        assertEquals("Logistic Regression", ModelFactory.create("logistic").getName());
        assertEquals("Rule-Based Score", ModelFactory.create("rule").getName());
    }
}
