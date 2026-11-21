package com.heartrisk.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.heartrisk.model.Patient;
import com.heartrisk.model.Doctor;
import com.heartrisk.model.RiskAssessment;
import com.heartrisk.prediction.ModelFactory;
import com.heartrisk.prediction.PredictionModel;
import com.heartrisk.service.ModelCatalog;
import com.heartrisk.service.PatientService;
import com.heartrisk.service.PredictionService;
import com.heartrisk.service.RecommendationService;
import com.heartrisk.service.ReportService;
import io.javalin.http.Context;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** One method per REST endpoint; each reads the request, calls a service and writes the response. */
public class ApiHandlers {

    private static final String DEFAULT_MODEL = "logistic";

    private final PatientService patients;
    private final PredictionService predictions;
    private final ModelCatalog catalog;
    private final RecommendationService recommendations;
    private final ReportService reports;

    /**
     * Wires the handlers to the services they use.
     *
     * @param patients        patient registration and lookup
     * @param predictions     assessment creation
     * @param catalog         trained models and metrics
     * @param recommendations advice generator
     * @param reports         plain-text report generator
     */
    public ApiHandlers(PatientService patients, PredictionService predictions, ModelCatalog catalog,
                       RecommendationService recommendations, ReportService reports) {
        this.patients = patients;
        this.predictions = predictions;
        this.catalog = catalog;
        this.recommendations = recommendations;
        this.reports = reports;
    }

    /** GET /api/health - liveness check. @param ctx request context */
    public void health(Context ctx) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "UP");
        body.put("service", "Heart Disease Risk Prediction System");
        body.put("models", ModelFactory.SUPPORTED_KEYS);
        ctx.json(body);
    }

    /** GET /api/doctors - available doctors for patient assignment. */
    public void doctors(Context ctx) {
        List<Map<String, Object>> items = patients.getDoctors().stream().map(doctor -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", doctor.getId());
            item.put("name", doctor.getName());
            item.put("specialty", doctor.getSpecialty());
            return item;
        }).toList();
        ctx.json(Map.of("doctors", items));
    }

    /** GET /api/patients - all registered patients with their assessment history. */
    public void allPatients(Context ctx) {
        Map<String, String> doctorNames = patients.getDoctors().stream().collect(
                java.util.stream.Collectors.toMap(Doctor::getId, Doctor::getName));
        List<Map<String, Object>> items = patients.getAllPatients().stream().map(patient -> {
            Map<String, Object> item = new LinkedHashMap<>(ResponseMapper.patient(patient));
            item.put("doctorName", doctorNames.getOrDefault(patient.getDoctorId(), "Unknown doctor"));
            item.put("assessments", patients.getHistory(patient.getId()).stream()
                    .map(ResponseMapper::assessment).toList());
            return item;
        }).toList();
        ctx.json(Map.of("patients", items));
    }

    /** POST /api/patients - registers a patient with the 13 features. @param ctx request context */
    public void createPatient(Context ctx) {
        JsonNode body = RequestParser.readBody(ctx.body());
        Patient patient = patients.register(RequestParser.requiredText(body, "name"),
                RequestParser.requiredText(body, "email"), RequestParser.readFeatures(body),
                RequestParser.requiredText(body, "doctorId"));
        ctx.status(201).json(ResponseMapper.patient(patient));
    }

    /** POST /api/predict?model=logistic|rule - assesses an existing patient. @param ctx request context */
    public void predict(Context ctx) {
        PredictionModel model = catalog.get(modelKey(ctx));
        JsonNode body = RequestParser.readBody(ctx.body());
        Patient patient = patients.getPatient(RequestParser.requiredText(body, "patientId"));
        RiskAssessment assessment = predictions.assessWith(model, patient);
        List<String> advice = recommendations.recommend(assessment.getLevel(), patient.getHealthRecord());
        ctx.json(ResponseMapper.prediction(assessment, advice));
    }

    /** GET /api/patients/{id}/history - past assessments of one patient. @param ctx request context */
    public void history(Context ctx) {
        String id = ctx.pathParam("id");
        List<Map<String, Object>> items = patients.getHistory(id).stream()
                .map(ResponseMapper::assessment).toList();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("patientId", id);
        body.put("assessments", items);
        ctx.json(body);
    }

    /** GET /api/model/metrics?model=logistic|rule - test-split metrics. @param ctx request context */
    public void metrics(Context ctx) {
        String key = modelKey(ctx);
        ctx.json(ResponseMapper.metrics(catalog.get(key).getName(), catalog.getMetrics(key), catalog));
    }

    /** GET /api/report/{assessmentId} - plain-text report. @param ctx request context */
    public void report(Context ctx) {
        RiskAssessment assessment = patients.getAssessment(ctx.pathParam("assessmentId"));
        Patient patient = patients.getPatient(assessment.getPatientId());
        ctx.contentType("text/plain; charset=utf-8").result(reports.generate(assessment, patient));
    }

    private static String modelKey(Context ctx) {
        String key = ctx.queryParam("model");
        return key == null ? DEFAULT_MODEL : key;
    }
}
