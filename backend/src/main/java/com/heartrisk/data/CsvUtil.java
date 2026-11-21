package com.heartrisk.data;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/** Minimal CSV helper: splits and joins one line, honouring double-quoted fields. */
final class CsvUtil {

    private CsvUtil() {
    }

    /**
     * Splits one CSV line into fields; quoted fields may contain commas and doubled quotes.
     *
     * @param line a single line without line terminator
     * @return the fields, never empty
     */
    static List<String> split(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (quoted) {
                if (c == '"' && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else if (c == '"') {
                    quoted = false;
                } else {
                    current.append(c);
                }
            } else if (c == '"') {
                quoted = true;
            } else if (c == ',') {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());
        return fields;
    }

    /**
     * Joins values into one CSV line, quoting a value when it contains a comma or a quote.
     *
     * @param values the values to write
     * @return one CSV line without line terminator
     */
    static String join(List<?> values) {
        StringJoiner joiner = new StringJoiner(",");
        for (Object value : values) {
            String text = String.valueOf(value);
            boolean needsQuotes = text.indexOf(',') >= 0 || text.indexOf('"') >= 0;
            joiner.add(needsQuotes ? "\"" + text.replace("\"", "\"\"") + "\"" : text);
        }
        return joiner.toString();
    }
}
