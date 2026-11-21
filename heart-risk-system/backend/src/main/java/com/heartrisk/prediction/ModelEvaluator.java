package com.heartrisk.prediction;

import com.heartrisk.data.LabelledDataset;
import com.heartrisk.model.HealthRecord;
import java.util.List;

/** Measures a {@link PredictionModel} on a labelled test set. */
public class ModelEvaluator {

    /** A probability at or above this value is counted as "heart disease predicted". */
    public static final double DECISION_THRESHOLD = 0.5;

    /**
     * Predicts every test record and counts the confusion matrix.
     *
     * @param model the model to evaluate (already trained if it is {@link Trainable})
     * @param test  labelled test data
     * @return confusion matrix with accuracy, precision, recall and F1 accessors
     */
    public EvaluationResult evaluate(PredictionModel model, LabelledDataset test) {
        int tp = 0;
        int fp = 0;
        int tn = 0;
        int fn = 0;
        List<HealthRecord> records = test.records();
        for (int i = 0; i < records.size(); i++) {
            boolean predicted = model.predictProbability(records.get(i)) >= DECISION_THRESHOLD;
            boolean actual = test.labels()[i] == 1;
            if (predicted && actual) {
                tp++;
            } else if (predicted) {
                fp++;
            } else if (actual) {
                fn++;
            } else {
                tn++;
            }
        }
        return new EvaluationResult(tp, fp, tn, fn);
    }
}
