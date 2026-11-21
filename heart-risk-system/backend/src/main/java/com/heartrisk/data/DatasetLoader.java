package com.heartrisk.data;

import com.heartrisk.model.HealthRecord;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Reads the UCI Cleveland {@code heart.csv} into {@link HealthRecord} objects and labels.
 *
 * <p>Expected header: {@code age,sex,cp,trestbps,chol,fbs,restecg,thalach,exang,oldpeak,slope,ca,thal,target}
 * (column order does not matter, names do). A row containing a missing marker ({@code ?}, blank,
 * {@code NA}) in any column - in the original UCI files this occurs in {@code ca} and
 * {@code thal} - is skipped and counted in {@link LabelledDataset#skippedRows()}.</p>
 *
 * <p><b>Label direction:</b> in the supplied file {@code target = 0} marks patients WITH heart
 * disease (checked against the data: exercise angina, ST depression and vessel count are higher
 * for target 0). The loader therefore maps {@code target == 0} to label 1 ("disease present").</p>
 */
public class DatasetLoader {

    private static final String TARGET_COLUMN = "target";
    private static final int DISEASE_TARGET_VALUE = 0;

    /**
     * Loads and validates the whole file.
     *
     * @param path location of heart.csv
     * @return records with labels (1 = disease present)
     * @throws IOException if the file is missing, has no valid header, or a row is malformed
     */
    public LabelledDataset load(Path path) throws IOException {
        if (!Files.isRegularFile(path)) {
            throw new IOException("Dataset not found: " + path.toAbsolutePath()
                    + " (place heart.csv in the data/ folder)");
        }
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        if (lines.isEmpty()) {
            throw new IOException("Dataset is empty: " + path);
        }
        Map<String, Integer> column = readHeader(lines.get(0));
        int width = column.size();

        List<HealthRecord> records = new ArrayList<>();
        List<Integer> labels = new ArrayList<>();
        int skipped = 0;
        for (int i = 1; i < lines.size(); i++) {
            if (lines.get(i).isBlank()) {
                continue;
            }
            List<String> cells = CsvUtil.split(lines.get(i));
            if (cells.size() != width) {
                throw new IOException("Line " + (i + 1) + " has " + cells.size()
                        + " columns, expected " + width);
            }
            if (cells.stream().anyMatch(DatasetLoader::isMissing)) {
                skipped++;
                continue;
            }
            try {
                records.add(parseRecord(cells, column));
                labels.add(parseLabel(cells.get(column.get(TARGET_COLUMN))));
            } catch (IllegalArgumentException e) {
                throw new IOException("Invalid data on line " + (i + 1) + ": " + e.getMessage(), e);
            }
        }
        return new LabelledDataset(records, labels.stream().mapToInt(Integer::intValue).toArray(), skipped);
    }

    private static Map<String, Integer> readHeader(String headerLine) throws IOException {
        List<String> names = CsvUtil.split(headerLine.replace("﻿", ""));
        Map<String, Integer> column = new HashMap<>();
        for (int i = 0; i < names.size(); i++) {
            column.put(names.get(i).trim().toLowerCase(Locale.ROOT), i);
        }
        List<String> required = new ArrayList<>(HealthRecord.FEATURE_NAMES);
        required.add(TARGET_COLUMN);
        for (String name : required) {
            if (!column.containsKey(name)) {
                throw new IOException("Missing column '" + name + "' in dataset header");
            }
        }
        return column;
    }

    private static boolean isMissing(String cell) {
        String v = cell.trim();
        return v.isEmpty() || v.equals("?") || v.equalsIgnoreCase("na") || v.equalsIgnoreCase("nan");
    }

    private static HealthRecord parseRecord(List<String> cells, Map<String, Integer> column) {
        HealthRecord.Builder builder = HealthRecord.builder();
        for (String feature : HealthRecord.FEATURE_NAMES) {
            builder.feature(feature, Double.parseDouble(cells.get(column.get(feature)).trim()));
        }
        return builder.build();
    }

    private static int parseLabel(String cell) {
        double target = Double.parseDouble(cell.trim());
        if (target != 0.0 && target != 1.0) {
            throw new IllegalArgumentException("target must be 0 or 1 but was " + cell);
        }
        return target == DISEASE_TARGET_VALUE ? 1 : 0;
    }
}
