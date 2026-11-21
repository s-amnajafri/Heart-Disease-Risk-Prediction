package com.heartrisk.service;

import com.heartrisk.model.HealthRecord;
import com.heartrisk.model.Patient;
import com.heartrisk.model.RiskAssessment;
import java.util.Locale;
import java.util.Map;

/** Creates the downloadable plain-text report for one assessment. */
public class ReportService {

    private final RecommendationService recommendations;

    /**
     * Creates the service.
     *
     * @param recommendations source of the advice section
     */
    public ReportService(RecommendationService recommendations) {
        this.recommendations = recommendations;
    }

    /**
     * Renders the report.
     *
     * @param assessment the assessment to describe
     * @param patient    the assessed patient
     * @return report text with patient data, features, result, recommendations and disclaimer
     */
    public String generate(RiskAssessment assessment, Patient patient) {
        StringBuilder text = new StringBuilder();
        text.append("HEART DISEASE RISK REPORT\n=========================\n");
        text.append("Assessment ID : ").append(assessment.getId()).append('\n');
        text.append("Date (UTC)    : ").append(assessment.getTimestamp()).append("\n\n");
        text.append("PATIENT\n-------\n");
        text.append("ID    : ").append(patient.getId()).append('\n');
        text.append("Name  : ").append(patient.getName()).append('\n');
        text.append("Email : ").append(patient.getEmail()).append("\n\n");
        text.append("HEALTH RECORD\n-------------\n");
        for (Map.Entry<String, Object> entry : patient.getHealthRecord().toMap().entrySet()) {
            text.append(String.format("%-9s: %s%n", entry.getKey(), entry.getValue()));
        }
        text.append("\nRESULT\n------\n");
        text.append("Model       : ").append(assessment.getModelName()).append('\n');
        text.append(String.format(Locale.ROOT, "Probability : %.1f%%%n", assessment.getProbability() * 100.0));
        text.append("Risk level  : ").append(assessment.getLevel()).append("\n\n");
        text.append("RECOMMENDATIONS\n---------------\n");
        HealthRecord record = patient.getHealthRecord();
        for (String line : recommendations.recommend(assessment.getLevel(), record)) {
            text.append("- ").append(line).append('\n');
        }
        text.append('\n').append(RecommendationService.DISCLAIMER).append('\n');
        return text.toString();
    }
}
