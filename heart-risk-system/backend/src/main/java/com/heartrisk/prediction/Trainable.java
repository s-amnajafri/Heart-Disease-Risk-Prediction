package com.heartrisk.prediction;

import com.heartrisk.data.LabelledDataset;

/**
 * Marks a {@link PredictionModel} that must learn its parameters from labelled data before use.
 * Rule-based models do not implement it because they need no training.
 */
public interface Trainable {

    /**
     * Fits the model to the given training data.
     *
     * @param trainingData labelled records (label 1 = heart disease present)
     * @throws IllegalArgumentException if the data set is empty
     */
    void train(LabelledDataset trainingData);
}
