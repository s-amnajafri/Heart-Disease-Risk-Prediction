package com.heartrisk.observer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.heartrisk.model.Doctor;
import com.heartrisk.model.RiskAssessment;
import java.time.Instant;
import org.junit.jupiter.api.Test;

/** The Observer pattern: only HIGH-risk assessments raise an alert. */
class HighRiskNotifierTest {

    private static RiskAssessment assessment(String id, double probability) {
        return new RiskAssessment(id, "P-0001", probability, "Test Model", Instant.now());
    }

    @Test
    void onlyHighRiskAssessmentsRaiseAlerts() {
        HighRiskNotifier notifier = new HighRiskNotifier(
                new Doctor("D-001", "Dr Test", "doctor@example.com", "Cardiology"));
        notifier.onAssessmentCreated(assessment("A-1", 0.10));
        notifier.onAssessmentCreated(assessment("A-2", 0.50));
        assertEquals(0, notifier.getAlertCount());
        notifier.onAssessmentCreated(assessment("A-3", 0.66));
        notifier.onAssessmentCreated(assessment("A-4", 0.95));
        assertEquals(2, notifier.getAlertCount());
    }
}
