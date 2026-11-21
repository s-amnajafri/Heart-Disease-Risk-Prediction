package com.heartrisk.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.heartrisk.TestRecords;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Validation rules of {@link HealthRecord}: every feature range and missing values. */
class HealthRecordTest {

    private static final Object[][] OUT_OF_RANGE = {
        {"age", 0.0}, {"age", 121.0}, {"sex", 2.0}, {"cp", 4.0}, {"trestbps", 79.0}, {"trestbps", 221.0},
        {"chol", 99.0}, {"chol", 601.0}, {"fbs", 2.0}, {"restecg", 3.0}, {"thalach", 59.0},
        {"thalach", 221.0}, {"exang", 2.0}, {"oldpeak", -0.1}, {"oldpeak", 7.1}, {"slope", 3.0},
        {"ca", 5.0}, {"thal", 4.0}
    };

    private static final Object[][] BOUNDARIES = {
        {"age", 1.0, 120.0}, {"sex", 0.0, 1.0}, {"cp", 0.0, 3.0}, {"trestbps", 80.0, 220.0},
        {"chol", 100.0, 600.0}, {"fbs", 0.0, 1.0}, {"restecg", 0.0, 2.0}, {"thalach", 60.0, 220.0},
        {"exang", 0.0, 1.0}, {"oldpeak", 0.0, 7.0}, {"slope", 0.0, 2.0}, {"ca", 0.0, 4.0}, {"thal", 0.0, 3.0}
    };

    @Test
    @DisplayName("a valid record is accepted")
    void validRecordIsAccepted() {
        assertDoesNotThrow(() -> {
            TestRecords.validBuilder().build();
        });
    }

    @Test
    @DisplayName("every out-of-range value is rejected with IllegalArgumentException naming the feature")
    void outOfRangeValuesAreRejected() {
        for (Object[] row : OUT_OF_RANGE) {
            String feature = (String) row[0];
            HealthRecord.Builder builder = TestRecords.validBuilder().feature(feature, (Double) row[1]);
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class, builder::build);
            assertTrue(e.getMessage().contains(feature), "message should name " + feature + ": " + e.getMessage());
        }
    }

    @Test
    @DisplayName("lowest and highest allowed values are accepted")
    void boundaryValuesAreAccepted() {
        for (Object[] row : BOUNDARIES) {
            for (int i = 1; i <= 2; i++) {
                HealthRecord.Builder builder = TestRecords.validBuilder().feature((String) row[0], (Double) row[i]);
                assertDoesNotThrow(() -> {
                    builder.build();
                }, row[0] + " boundary " + row[i]);
            }
        }
    }

    @Test
    @DisplayName("an empty builder and a builder missing one feature are rejected")
    void missingValuesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> HealthRecord.builder().build());
        Map<String, Object> full = TestRecords.validBuilder().build().toMap();
        for (String skipped : HealthRecord.FEATURE_NAMES) {
            HealthRecord.Builder builder = HealthRecord.builder();
            full.forEach((name, value) -> {
                if (!name.equals(skipped)) {
                    builder.feature(name, ((Number) value).doubleValue());
                }
            });
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class, builder::build);
            assertTrue(e.getMessage().contains(skipped), "message should name " + skipped);
        }
    }

    @Test
    @DisplayName("NaN is rejected")
    void nanIsRejected() {
        HealthRecord.Builder builder = TestRecords.validBuilder().oldpeak(Double.NaN);
        assertThrows(IllegalArgumentException.class, builder::build);
    }
}
