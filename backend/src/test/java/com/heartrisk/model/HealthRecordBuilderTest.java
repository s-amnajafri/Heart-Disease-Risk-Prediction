package com.heartrisk.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.heartrisk.TestRecords;
import org.junit.jupiter.api.Test;

/** Behaviour of the fluent {@link HealthRecord.Builder}. */
class HealthRecordBuilderTest {

    @Test
    void settersAreFluentAndReturnTheSameBuilder() {
        HealthRecord.Builder builder = HealthRecord.builder();
        assertSame(builder, builder.age(50));
        assertSame(builder, builder.oldpeak(1.5));
        assertSame(builder, builder.feature("thal", 2));
    }

    @Test
    void builtRecordExposesEveryValueThroughGetters() {
        HealthRecord r = HealthRecord.builder().age(61).sex(1).cp(2).trestbps(135).chol(250).fbs(1)
                .restecg(2).thalach(140).exang(1).oldpeak(2.5).slope(0).ca(3).thal(1).build();
        assertEquals(61, r.getAge());
        assertEquals(1, r.getSex());
        assertEquals(2, r.getCp());
        assertEquals(135, r.getTrestbps());
        assertEquals(250, r.getChol());
        assertEquals(1, r.getFbs());
        assertEquals(2, r.getRestecg());
        assertEquals(140, r.getThalach());
        assertEquals(1, r.getExang());
        assertEquals(2.5, r.getOldpeak(), 1e-9);
        assertEquals(0, r.getSlope());
        assertEquals(3, r.getCa());
        assertEquals(1, r.getThal());
    }

    @Test
    void featureVectorFollowsTheFixedColumnOrder() {
        double[] expected = {50, 1, 0, 120, 200, 0, 1, 150, 0, 1.0, 1, 0, 2};
        assertArrayEquals(expected, TestRecords.validBuilder().build().toFeatureVector(), 1e-9);
    }

    @Test
    void sameBuilderCreatesIndependentRecords() {
        HealthRecord.Builder builder = TestRecords.validBuilder();
        HealthRecord first = builder.build();
        HealthRecord second = builder.age(70).build();
        assertNotSame(first, second);
        assertEquals(50, first.getAge());
        assertEquals(70, second.getAge());
    }

    @Test
    void featureByNameRejectsUnknownNamesAndFractions() {
        assertThrows(IllegalArgumentException.class, () -> HealthRecord.builder().feature("weight", 70));
        assertThrows(IllegalArgumentException.class, () -> HealthRecord.builder().feature("age", 50.5));
    }
}
