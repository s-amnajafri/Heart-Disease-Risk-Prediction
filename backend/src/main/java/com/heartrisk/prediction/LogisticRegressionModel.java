package com.heartrisk.prediction;

import com.heartrisk.data.LabelledDataset;
import com.heartrisk.model.HealthRecord;
import java.util.List;

/**
 * Logistic regression implemented from scratch: z-score standardisation, sigmoid activation
 * and batch gradient descent on the log-loss. Means and standard deviations are computed on the
 * training data only and stored, so new patients are scaled exactly like the training rows.
 *
 * <p>Implements {@link PredictionModel} (a Strategy) and {@link Trainable}.</p>
 */
public class LogisticRegressionModel implements PredictionModel, Trainable {

    private static final String NAME = "Logistic Regression";
    private static final double MIN_STD_DEV = 1e-12;

    private final double learningRate;
    private final int epochs;
    private double[] means;
    private double[] stdDevs;
    private double[] weights;
    private double bias;
    private volatile boolean trained;

    /**
     * Creates an untrained model.
     *
     * @param learningRate gradient-descent step size (positive)
     * @param epochs       number of full passes over the training data (positive)
     */
    public LogisticRegressionModel(double learningRate, int epochs) {
        if (learningRate <= 0 || epochs <= 0) {
            throw new IllegalArgumentException("learningRate and epochs must be positive");
        }
        this.learningRate = learningRate;
        this.epochs = epochs;
    }

    @Override
    public void train(LabelledDataset trainingData) {
        int n = trainingData.size();
        if (n == 0) {
            throw new IllegalArgumentException("Training data must not be empty");
        }
        double[][] x = toMatrix(trainingData.records());
        int features = x[0].length;
        double[] mean = columnMeans(x);
        double[] std = columnStdDevs(x, mean);
        standardise(x, mean, std);

        int[] y = trainingData.labels();
        double[] w = new double[features];
        double b = 0.0;
        for (int epoch = 0; epoch < epochs; epoch++) {
            double[] gradW = new double[features];
            double gradB = 0.0;
            for (int i = 0; i < n; i++) {
                double error = sigmoid(dot(w, x[i]) + b) - y[i];
                for (int j = 0; j < features; j++) {
                    gradW[j] += error * x[i][j];
                }
                gradB += error;
            }
            for (int j = 0; j < features; j++) {
                w[j] -= learningRate * gradW[j] / n;
            }
            b -= learningRate * gradB / n;
        }
        this.means = mean;
        this.stdDevs = std;
        this.weights = w;
        this.bias = b;
        this.trained = true;
    }

    @Override
    public double predictProbability(HealthRecord record) {
        if (!trained) {
            throw new IllegalStateException("Model has not been trained yet");
        }
        double[] v = record.toFeatureVector();
        double z = bias;
        for (int j = 0; j < v.length; j++) {
            z += weights[j] * (v[j] - means[j]) / stdDevs[j];
        }
        return sigmoid(z);
    }

    @Override
    public String getName() {
        return NAME;
    }

    private static double sigmoid(double z) {
        return 1.0 / (1.0 + Math.exp(-z));
    }

    private static double dot(double[] a, double[] b) {
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            sum += a[i] * b[i];
        }
        return sum;
    }

    private static double[][] toMatrix(List<HealthRecord> records) {
        double[][] x = new double[records.size()][];
        for (int i = 0; i < x.length; i++) {
            x[i] = records.get(i).toFeatureVector();
        }
        return x;
    }

    private static double[] columnMeans(double[][] x) {
        double[] mean = new double[x[0].length];
        for (double[] row : x) {
            for (int j = 0; j < mean.length; j++) {
                mean[j] += row[j] / x.length;
            }
        }
        return mean;
    }

    private static double[] columnStdDevs(double[][] x, double[] mean) {
        double[] std = new double[mean.length];
        for (double[] row : x) {
            for (int j = 0; j < std.length; j++) {
                double d = row[j] - mean[j];
                std[j] += d * d / x.length;
            }
        }
        for (int j = 0; j < std.length; j++) {
            std[j] = Math.sqrt(std[j]);
            if (std[j] < MIN_STD_DEV) {
                std[j] = 1.0;
            }
        }
        return std;
    }

    private static void standardise(double[][] x, double[] mean, double[] std) {
        for (double[] row : x) {
            for (int j = 0; j < row.length; j++) {
                row[j] = (row[j] - mean[j]) / std[j];
            }
        }
    }
}
