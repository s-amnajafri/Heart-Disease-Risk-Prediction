package com.heartrisk;

import com.heartrisk.model.HealthRecord;

/** Ready-made builders shared by the tests. */
public final class TestRecords {

    private TestRecords() {
    }

    /** @return builder holding a valid, average patient */
    public static HealthRecord.Builder validBuilder() {
        return HealthRecord.builder().age(50).sex(1).cp(0).trestbps(120).chol(200).fbs(0)
                .restecg(1).thalach(150).exang(0).oldpeak(1.0).slope(1).ca(0).thal(2);
    }

    /** @return builder for a patient with no rule-based risk points (score 0.0) */
    public static HealthRecord.Builder lowRiskBuilder() {
        return HealthRecord.builder().age(40).sex(0).cp(2).trestbps(110).chol(180).fbs(0)
                .restecg(0).thalach(170).exang(0).oldpeak(0.0).slope(2).ca(0).thal(2);
    }

    /** @return builder for a patient with every rule-based risk point (score 1.0) */
    public static HealthRecord.Builder highRiskBuilder() {
        return HealthRecord.builder().age(65).sex(1).cp(0).trestbps(160).chol(300).fbs(1)
                .restecg(1).thalach(100).exang(1).oldpeak(3.5).slope(0).ca(3).thal(3);
    }
}
