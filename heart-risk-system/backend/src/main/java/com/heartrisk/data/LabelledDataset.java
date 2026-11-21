package com.heartrisk.data;

import com.heartrisk.model.HealthRecord;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Health records paired with their labels (1 = heart disease present, 0 = absent).
 *
 * @param records     the records
 * @param labels      one label per record, same order
 * @param skippedRows number of CSV rows ignored while loading because of missing values
 */
public record LabelledDataset(List<HealthRecord> records, int[] labels, int skippedRows) {

    /** Validates that records and labels have the same length and copies the inputs. */
    public LabelledDataset {
        if (records.size() != labels.length) {
            throw new IllegalArgumentException("records and labels must have the same length");
        }
        records = List.copyOf(records);
        labels = labels.clone();
    }

    /** @return number of labelled records */
    public int size() {
        return records.size();
    }

    /**
     * Shuffles with a fixed seed and splits into a training and a test part.
     *
     * @param trainFraction share of rows used for training, strictly between 0 and 1
     * @param seed          random seed, so the split is reproducible
     * @return the two parts
     */
    public Split split(double trainFraction, long seed) {
        if (trainFraction <= 0.0 || trainFraction >= 1.0) {
            throw new IllegalArgumentException("trainFraction must be between 0 and 1");
        }
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < size(); i++) {
            order.add(i);
        }
        Collections.shuffle(order, new Random(seed));
        int trainSize = (int) Math.round(size() * trainFraction);
        return new Split(subset(order.subList(0, trainSize)),
                subset(order.subList(trainSize, order.size())));
    }

    private LabelledDataset subset(List<Integer> indices) {
        List<HealthRecord> subRecords = new ArrayList<>();
        int[] subLabels = new int[indices.size()];
        for (int i = 0; i < indices.size(); i++) {
            subRecords.add(records.get(indices.get(i)));
            subLabels[i] = labels[indices.get(i)];
        }
        return new LabelledDataset(subRecords, subLabels, 0);
    }

    /**
     * Result of {@link #split(double, long)}.
     *
     * @param train the training part
     * @param test  the test part
     */
    public record Split(LabelledDataset train, LabelledDataset test) {
    }
}
