package com.heartrisk.service;

import com.heartrisk.model.HealthRecord;
import com.heartrisk.model.RiskLevel;
import java.util.ArrayList;
import java.util.List;

/** Produces general, educational advice from a risk level and the patient's risk factors. */
public class RecommendationService {

    /** Disclaimer shown on every report and in the user interface. */
    public static final String DISCLAIMER = "Educational tool only, not a medical diagnosis.";

    /**
     * Builds the recommendation list.
     *
     * @param level  risk level of the assessment
     * @param record the patient's health record
     * @return advice lines, general advice first, then one line per risk factor present
     */
    public List<String> recommend(RiskLevel level, HealthRecord record) {
        List<String> advice = new ArrayList<>();
        switch (level) {
            case LOW -> advice.add("Keep a balanced diet, stay physically active and have routine check-ups.");
            case MEDIUM -> advice.add("Arrange a check-up with a doctor to review these results and risk factors.");
            case HIGH -> advice.add("See a doctor or cardiologist soon to discuss these results in person.");
        }
        if (record.getChol() > 240) {
            advice.add("Cholesterol is above 240 mg/dl: discuss diet and lipid management with a doctor.");
        }
        if (record.getTrestbps() > 140) {
            advice.add("Resting blood pressure is above 140 mm Hg: have it measured and reviewed regularly.");
        }
        if (record.getFbs() == 1) {
            advice.add("Fasting blood sugar is above 120 mg/dl: ask about screening for diabetes.");
        }
        if (record.getExang() == 1) {
            advice.add("Exercise-induced chest pain was reported: have it medically assessed before strenuous exercise.");
        }
        if (record.getAge() > 55) {
            advice.add("Age is above 55: regular cardiovascular screening is recommended.");
        }
        return advice;
    }
}
