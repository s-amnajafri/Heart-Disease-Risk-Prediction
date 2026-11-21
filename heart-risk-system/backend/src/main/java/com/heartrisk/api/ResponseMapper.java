package com.heartrisk.api;

import com.heartrisk.model.Patient;
import com.heartrisk.model.RiskAssessment;
import com.heartrisk.prediction.EvaluationResult;
import com.heartrisk.service.ModelCatalog;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Converts domain objects to plain maps that Jackson serialises as JSON. */
final class ResponseMapper {

    private ResponseMapper() {
    }

    /** @param patient the patient @return JSON map of the patient and the features it owns */
    static Map<String, Object> patient(Patient patient) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", patient.getId());
        map.put("name", patient.getName());
        map.put("email", patient.getEmail());
        map.put("role", patient.getRole());
        map.put("doctorId", patient.getDoctorId());
        map.put("features", patient.getHealthRecord().toMap());
        return map;
    }

    /** @param a the assessment @return JSON map without recommendations */
    static Map<String, Object> assessment(RiskAssessment a) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("assessmentId", a.getId());
        map.put("patientId", a.getPatientId());
        map.put("probability", a.getProbability());
        map.put("level", a.getLevel().name());
        map.put("model", a.getModelName());
        map.put("timestamp", a.getTimestamp().toString());
        return map;
    }

    /**
     * @param a               the assessment
     * @param recommendations advice lines
     * @return JSON map of the prediction response
     */
    static Map<String, Object> prediction(RiskAssessment a, List<String> recommendations) {
        Map<String, Object> map = assessment(a);
        map.put("recommendations", recommendations);
        return map;
    }

    /**
     * @param modelName display name of the model
     * @param result    metrics on the test split
     * @param catalog   source of the split sizes
     * @return JSON map of the metrics response
     */
    static Map<String, Object> metrics(String modelName, EvaluationResult result, ModelCatalog catalog) {
        Map<String, Object> matrix = new LinkedHashMap<>();
        matrix.put("truePositives", result.truePositives());
        matrix.put("falsePositives", result.falsePositives());
        matrix.put("trueNegatives", result.trueNegatives());
        matrix.put("falseNegatives", result.falseNegatives());
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("model", modelName);
        map.put("accuracy", result.accuracy());
        map.put("precision", result.precision());
        map.put("recall", result.recall());
        map.put("f1", result.f1());
        map.put("confusionMatrix", matrix);
        map.put("trainSamples", catalog.getTrainSize());
        map.put("testSamples", catalog.getTestSize());
        return map;
    }
}
