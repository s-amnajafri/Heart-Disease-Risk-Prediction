package com.heartrisk.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** Threshold behaviour of {@link RiskLevel}: below 0.33 LOW, below 0.66 MEDIUM, otherwise HIGH. */
class RiskLevelTest {

    @Test
    void valuesBelowThirtyThreePercentAreLow() {
        assertEquals(RiskLevel.LOW, RiskLevel.fromProbability(0.0));
        assertEquals(RiskLevel.LOW, RiskLevel.fromProbability(0.329));
    }

    @Test
    void valuesFromThirtyThreeToBelowSixtySixPercentAreMedium() {
        assertEquals(RiskLevel.MEDIUM, RiskLevel.fromProbability(0.33));
        assertEquals(RiskLevel.MEDIUM, RiskLevel.fromProbability(0.5));
        assertEquals(RiskLevel.MEDIUM, RiskLevel.fromProbability(0.659));
    }

    @Test
    void valuesFromSixtySixPercentUpAreHigh() {
        assertEquals(RiskLevel.HIGH, RiskLevel.fromProbability(0.66));
        assertEquals(RiskLevel.HIGH, RiskLevel.fromProbability(1.0));
    }

    @Test
    void invalidProbabilitiesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> RiskLevel.fromProbability(-0.01));
        assertThrows(IllegalArgumentException.class, () -> RiskLevel.fromProbability(1.01));
        assertThrows(IllegalArgumentException.class, () -> RiskLevel.fromProbability(Double.NaN));
    }
}
