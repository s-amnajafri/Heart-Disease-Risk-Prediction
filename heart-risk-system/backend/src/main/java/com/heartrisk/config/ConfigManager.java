package com.heartrisk.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Central, read-only application configuration (port, dataset path, learning rate,
 * epochs and train/test split seed).
 *
 * <p><b>DESIGN PATTERN: Singleton (thread-safe).</b> The instance is created lazily by the
 * initialization-on-demand holder idiom: the JVM initialises the nested {@code Holder} class
 * exactly once and in a thread-safe way, so no explicit synchronisation is needed. All fields
 * are final, therefore the shared instance is immutable and safe to read from any thread.</p>
 */
public final class ConfigManager {

    private static final String PRIMARY_DATASET = "../data/heart.csv";
    private static final String FALLBACK_DATASET = "data/heart.csv";

    private final int port = 7071;
    private final double learningRate = 0.1;
    private final int epochs = 2000;
    private final long splitSeed = 42L;
    private final Path datasetPath;

    private ConfigManager() {
        this.datasetPath = resolveDatasetPath();
    }

    /** Holder class: loaded on first call of {@link #getInstance()}, exactly once. */
    private static final class Holder {
        private static final ConfigManager INSTANCE = new ConfigManager();
    }

    /**
     * Returns the one shared configuration object.
     *
     * @return the singleton instance
     */
    public static ConfigManager getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Uses {@code ../data/heart.csv} (jar started from {@code backend/}); if that file does not
     * exist it falls back to {@code data/heart.csv} (jar started from the project root).
     */
    private static Path resolveDatasetPath() {
        Path primary = Paths.get(PRIMARY_DATASET);
        if (Files.isRegularFile(primary)) {
            return primary;
        }
        Path fallback = Paths.get(FALLBACK_DATASET);
        return Files.isRegularFile(fallback) ? fallback : primary;
    }

    /** @return HTTP port of the REST API */
    public int getPort() {
        return port;
    }

    /** @return path of heart.csv (resolved once at start-up) */
    public Path getDatasetPath() {
        return datasetPath;
    }

    /** @return directory that holds heart.csv and the generated patients.csv / assessments.csv */
    public Path getDataDirectory() {
        Path parent = datasetPath.getParent();
        return parent == null ? Paths.get(".") : parent;
    }

    /** @return gradient-descent learning rate for logistic regression */
    public double getLearningRate() {
        return learningRate;
    }

    /** @return number of gradient-descent epochs */
    public int getEpochs() {
        return epochs;
    }

    /** @return random seed used for the 80/20 train/test split */
    public long getSplitSeed() {
        return splitSeed;
    }
}
