package com.heartrisk.prediction;

import com.heartrisk.config.ConfigManager;
import java.util.List;
import java.util.Locale;

/**
 * Creates {@link PredictionModel} objects from a short key.
 *
 * <p><b>DESIGN PATTERN: Factory.</b> Callers ask for {@code "logistic"} or {@code "rule"} and
 * receive the matching implementation without referring to the concrete class. Hyper-parameters
 * of the logistic model come from the {@link ConfigManager} singleton. The returned logistic
 * model is untrained; see {@link Trainable}.</p>
 */
public final class ModelFactory {

    /** Keys accepted by {@link #create(String)}. */
    public static final List<String> SUPPORTED_KEYS = List.of("logistic", "rule");

    private ModelFactory() {
    }

    /**
     * Builds a new model instance.
     *
     * @param key {@code "logistic"} or {@code "rule"} (case-insensitive)
     * @return a new model
     * @throws IllegalArgumentException if the key is null or unknown
     */
    public static PredictionModel create(String key) {
        if (key == null) {
            throw new IllegalArgumentException("model key must not be null");
        }
        ConfigManager config = ConfigManager.getInstance();
        return switch (key.trim().toLowerCase(Locale.ROOT)) {
            case "logistic" -> new LogisticRegressionModel(config.getLearningRate(), config.getEpochs());
            case "rule" -> new RuleBasedModel();
            default -> throw new IllegalArgumentException(
                    "Unknown model '" + key + "'. Use one of: " + String.join(", ", SUPPORTED_KEYS));
        };
    }
}
