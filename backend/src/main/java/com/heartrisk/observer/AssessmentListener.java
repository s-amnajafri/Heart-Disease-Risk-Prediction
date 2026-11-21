package com.heartrisk.observer;

import com.heartrisk.model.RiskAssessment;

/**
 * Observer interface: implementations are told whenever a new {@link RiskAssessment} is created.
 *
 * <p><b>DESIGN PATTERN: Observer.</b> {@code PredictionService} is the subject; it keeps a list
 * of listeners and calls {@link #onAssessmentCreated(RiskAssessment)} on each of them.</p>
 */
public interface AssessmentListener {

    /**
     * Called after an assessment has been created and stored.
     *
     * @param assessment the new assessment
     */
    void onAssessmentCreated(RiskAssessment assessment);
}
