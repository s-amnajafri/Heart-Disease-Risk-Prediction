package com.heartrisk.service;

import com.heartrisk.config.ConfigManager;
import com.heartrisk.data.LabelledDataset;
import com.heartrisk.prediction.EvaluationResult;
import com.heartrisk.prediction.ModelEvaluator;
import com.heartrisk.prediction.ModelFactory;
import com.heartrisk.prediction.PredictionModel;
import com.heartrisk.prediction.Trainable;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Holds the trained models and their test-set metrics. Built once at start-up: the data is split
 * 80/20 with the configured seed, every model from {@link ModelFactory} is created, trainable
 * models are trained on the 80 % part, and all models are evaluated on the 20 % part.
 */
public final class ModelCatalog {

    private static final double TRAIN_FRACTION = 0.8;

    private final Map<String, PredictionModel> models = new LinkedHashMap<>();
    private final Map<String, EvaluationResult> metrics = new LinkedHashMap<>();
    private final int trainSize;
    private final int testSize;

    private ModelCatalog(int trainSize, int testSize) {
        this.trainSize = trainSize;
        this.testSize = testSize;
    }

    /**
     * Trains and evaluates all models.
     *
     * @param dataset the full labelled data set
     * @param config  supplies the split seed
     * @return the ready-to-use catalog
     */
    public static ModelCatalog trainFrom(LabelledDataset dataset, ConfigManager config) {
        LabelledDataset.Split split = dataset.split(TRAIN_FRACTION, config.getSplitSeed());
        ModelCatalog catalog = new ModelCatalog(split.train().size(), split.test().size());
        ModelEvaluator evaluator = new ModelEvaluator();
        for (String key : ModelFactory.SUPPORTED_KEYS) {
            PredictionModel model = ModelFactory.create(key);
            if (model instanceof Trainable trainable) {
                trainable.train(split.train());
            }
            catalog.models.put(key, model);
            catalog.metrics.put(key, evaluator.evaluate(model, split.test()));
        }
        return catalog;
    }

    /**
     * Returns a model by key.
     *
     * @param key {@code "logistic"} or {@code "rule"}
     * @return the trained model
     * @throws IllegalArgumentException if the key is unknown
     */
    public PredictionModel get(String key) {
        PredictionModel model = models.get(normalise(key));
        if (model == null) {
            throw new IllegalArgumentException(
                    "Unknown model '" + key + "'. Use one of: " + String.join(", ", models.keySet()));
        }
        return model;
    }

    /**
     * Returns the test-set metrics of a model.
     *
     * @param key {@code "logistic"} or {@code "rule"}
     * @return confusion matrix and derived metrics
     * @throws IllegalArgumentException if the key is unknown
     */
    public EvaluationResult getMetrics(String key) {
        get(key);
        return metrics.get(normalise(key));
    }

    /** @return number of rows in the training split */
    public int getTrainSize() {
        return trainSize;
    }

    /** @return number of rows in the test split */
    public int getTestSize() {
        return testSize;
    }

    private static String normalise(String key) {
        return key == null ? "" : key.trim().toLowerCase(Locale.ROOT);
    }
}
