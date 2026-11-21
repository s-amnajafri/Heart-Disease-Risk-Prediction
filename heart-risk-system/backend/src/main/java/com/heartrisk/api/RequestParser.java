package com.heartrisk.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heartrisk.model.HealthRecord;

/** Reads and checks JSON request bodies; every problem becomes an IllegalArgumentException (HTTP 400). */
final class RequestParser {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private RequestParser() {
    }

    /**
     * Parses the body as a JSON object.
     *
     * @param body raw request body
     * @return the JSON object
     */
    static JsonNode readBody(String body) {
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("Request body is required");
        }
        try {
            JsonNode node = MAPPER.readTree(body);
            if (node == null || !node.isObject()) {
                throw new IllegalArgumentException("Request body must be a JSON object");
            }
            return node;
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Request body is not valid JSON");
        }
    }

    /**
     * Reads a required, non-blank text field.
     *
     * @param node  JSON object
     * @param field field name
     * @return trimmed text
     */
    static String requiredText(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || !value.isTextual() || value.asText().isBlank()) {
            throw new IllegalArgumentException(field + " is required and must be text");
        }
        return value.asText().trim();
    }

    /**
     * Reads the {@code features} object into a builder (range checks happen in {@code build()}).
     *
     * @param node JSON object that contains {@code features}
     * @return builder with all 13 features set
     */
    static HealthRecord.Builder readFeatures(JsonNode node) {
        JsonNode features = node.get("features");
        if (features == null || !features.isObject()) {
            throw new IllegalArgumentException("features object is required");
        }
        HealthRecord.Builder builder = HealthRecord.builder();
        for (String name : HealthRecord.FEATURE_NAMES) {
            JsonNode value = features.get(name);
            if (value == null || !value.isNumber()) {
                throw new IllegalArgumentException(name + " is required and must be a number");
            }
            builder.feature(name, value.asDouble());
        }
        return builder;
    }
}
