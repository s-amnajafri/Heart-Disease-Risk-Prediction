package com.heartrisk.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.heartrisk.TestRecords;
import com.heartrisk.data.CsvPatientRepository;
import com.heartrisk.model.HealthRecord;
import com.heartrisk.model.Patient;
import com.heartrisk.model.RiskAssessment;
import com.heartrisk.model.RiskLevel;
import com.heartrisk.observer.AssessmentListener;
import com.heartrisk.prediction.PredictionModel;
import com.heartrisk.prediction.RuleBasedModel;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Strategy (swapping the model at runtime) and Observer (notification) in {@link PredictionService}. */
class PredictionServiceTest {

    @TempDir
    Path tempDir;

    /** A fixed-probability model used to show that the strategy can be swapped. */
    private static final class FixedModel implements PredictionModel {
        @Override
        public double predictProbability(HealthRecord record) {
            return 0.10;
        }

        @Override
        public String getName() {
            return "Fixed Test Model";
        }
    }

    @Test
    void modelCanBeSwappedAtRuntime() throws IOException {
        CsvPatientRepository repo = new CsvPatientRepository(tempDir);
        Patient patient = new Patient("P-0001", "Ann", "ann@example.com", TestRecords.highRiskBuilder());
        repo.save(patient);
        PredictionService service = new PredictionService(new RuleBasedModel(), repo);

        RiskAssessment first = service.assess(patient);
        assertEquals(RiskLevel.HIGH, first.getLevel());
        assertEquals("Rule-Based Score", first.getModelName());

        FixedModel fixed = new FixedModel();
        service.setModel(fixed);
        assertSame(fixed, service.getModel());
        RiskAssessment second = service.assess(patient);
        assertEquals(RiskLevel.LOW, second.getLevel());
        assertEquals("Fixed Test Model", second.getModelName());
        assertEquals(2, repo.findAssessmentsByPatient("P-0001").size());
    }

    @Test
    void listenersAreNotifiedForEveryAssessment() throws IOException {
        CsvPatientRepository repo = new CsvPatientRepository(tempDir);
        Patient patient = new Patient("P-0001", "Ann", "ann@example.com", TestRecords.validBuilder());
        repo.save(patient);
        PredictionService service = new PredictionService(new RuleBasedModel(), repo);
        List<String> seen = new ArrayList<>();
        AssessmentListener listener = a -> seen.add(a.getId());
        service.addListener(listener);

        service.assess(patient);
        service.assessWith(new FixedModel(), patient);
        assertEquals(List.of("A-0001", "A-0002"), seen);
    }
}
