package com.heartrisk.prediction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.heartrisk.TestRecords;
import com.heartrisk.config.ConfigManager;
import com.heartrisk.data.DatasetLoader;
import com.heartrisk.data.LabelledDataset;
import com.heartrisk.service.ModelCatalog;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

/** Logistic regression trained exactly as in production (80/20 split, seed 42). */
class LogisticRegressionAccuracyTest {

    @Test
    void accuracyOnTheTestSplitIsAboveSeventyFivePercent() throws IOException {
        ConfigManager config = ConfigManager.getInstance();
        Assumptions.assumeTrue(Files.isRegularFile(config.getDatasetPath()),
                "heart.csv not found in data/ - accuracy test skipped");
        LabelledDataset data = new DatasetLoader().load(config.getDatasetPath());
        ModelCatalog catalog = ModelCatalog.trainFrom(data, config);
        double accuracy = catalog.getMetrics("logistic").accuracy();
        assertTrue(accuracy > 0.75, "logistic accuracy was " + accuracy);
        assertEquals(data.size(), catalog.getTrainSize() + catalog.getTestSize());
    }

    @Test
    void predictingBeforeTrainingFails() {
        LogisticRegressionModel model = new LogisticRegressionModel(0.1, 10);
        assertThrows(IllegalStateException.class,
                () -> model.predictProbability(TestRecords.validBuilder().build()));
    }

    @Test
    void trainingOnEmptyDataFails() {
        LogisticRegressionModel model = new LogisticRegressionModel(0.1, 10);
        LabelledDataset empty = new LabelledDataset(java.util.List.of(), new int[0], 0);
        assertThrows(IllegalArgumentException.class, () -> model.train(empty));
    }

    @Test
    void invalidHyperParametersAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new LogisticRegressionModel(0.0, 10));
        assertThrows(IllegalArgumentException.class, () -> new LogisticRegressionModel(0.1, 0));
    }
}
