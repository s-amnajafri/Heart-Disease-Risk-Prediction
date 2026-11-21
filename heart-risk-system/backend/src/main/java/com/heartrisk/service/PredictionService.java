package com.heartrisk.service;

import com.heartrisk.data.PatientRepository;
import com.heartrisk.model.Patient;
import com.heartrisk.model.RiskAssessment;
import com.heartrisk.observer.AssessmentListener;
import com.heartrisk.prediction.PredictionModel;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Creates risk assessments.
 *
 * <p><b>DESIGN PATTERN: Strategy (context).</b> The service holds one {@link PredictionModel}
 * and delegates the calculation to it; {@link #setModel(PredictionModel)} swaps the algorithm at
 * runtime without changing any other code.</p>
 *
 * <p><b>DESIGN PATTERN: Observer (subject).</b> Registered {@link AssessmentListener}s are
 * notified after each assessment is stored.</p>
 */
public class PredictionService {

    private final PatientRepository repository;
    private final List<AssessmentListener> listeners = new CopyOnWriteArrayList<>();
    private PredictionModel model;

    /**
     * Creates the service.
     *
     * @param initialModel model used until {@link #setModel(PredictionModel)} is called
     * @param repository   where assessments are stored
     */
    public PredictionService(PredictionModel initialModel, PatientRepository repository) {
        this.model = initialModel;
        this.repository = repository;
    }

    /**
     * Replaces the current strategy.
     *
     * @param newModel the model to use from now on
     */
    public synchronized void setModel(PredictionModel newModel) {
        this.model = newModel;
    }

    /** @return the model currently in use */
    public synchronized PredictionModel getModel() {
        return model;
    }

    /**
     * Registers an observer.
     *
     * @param listener listener to notify about new assessments
     */
    public void addListener(AssessmentListener listener) {
        listeners.add(listener);
    }

    /**
     * Predicts the patient's risk with the current model, stores the result and notifies listeners.
     *
     * @param patient the patient to assess
     * @return the stored assessment
     */
    public synchronized RiskAssessment assess(Patient patient) {
        double probability = model.predictProbability(patient.getHealthRecord());
        RiskAssessment assessment = new RiskAssessment(repository.nextAssessmentId(),
                patient.getId(), probability, model.getName(), Instant.now());
        repository.saveAssessment(assessment);
        listeners.forEach(l -> l.onAssessmentCreated(assessment));
        return assessment;
    }

    /**
     * Swaps the strategy and assesses in one atomic step (safe for concurrent requests).
     *
     * @param newModel model to use
     * @param patient  patient to assess
     * @return the stored assessment
     */
    public synchronized RiskAssessment assessWith(PredictionModel newModel, Patient patient) {
        setModel(newModel);
        return assess(patient);
    }
}
