package com.heartrisk.observer;

import com.heartrisk.model.Doctor;
import com.heartrisk.model.RiskAssessment;
import com.heartrisk.model.RiskLevel;
import com.heartrisk.data.PatientRepository;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Concrete observer that logs an alert for the responsible {@link Doctor} whenever a
 * HIGH-risk assessment is created. Assessments of other levels are ignored.
 *
 * <p><b>DESIGN PATTERN: Observer</b> (concrete observer).</p>
 */
public class HighRiskNotifier implements AssessmentListener {

    private static final Logger LOGGER = Logger.getLogger(HighRiskNotifier.class.getName());

    private final Doctor doctor;
    private final PatientRepository repository;
    private final Map<String, Doctor> doctors;
    private final AtomicInteger alertCount = new AtomicInteger();

    /**
     * Creates a notifier.
     *
     * @param doctor the doctor who is alerted
     */
    public HighRiskNotifier(Doctor doctor) {
        this.doctor = doctor;
        this.repository = null;
        this.doctors = Map.of();
    }

    /** Creates an observer that resolves the doctor assigned to each assessed patient. */
    public HighRiskNotifier(PatientRepository repository, List<Doctor> doctors) {
        this.doctor = null;
        this.repository = repository;
        this.doctors = doctors.stream().collect(Collectors.toMap(Doctor::getId, Function.identity()));
    }

    @Override
    public void onAssessmentCreated(RiskAssessment assessment) {
        if (assessment.getLevel() != RiskLevel.HIGH) {
            return;
        }
        alertCount.incrementAndGet();
        Doctor recipient = doctor;
        if (repository != null) {
            recipient = repository.findById(assessment.getPatientId())
                    .map(patient -> doctors.get(patient.getDoctorId())).orElse(null);
        }
        String recipientName = recipient == null ? "assigned doctor" : recipient.getName();
        LOGGER.warning(String.format(
                "HIGH RISK ALERT for %s: assessment %s, patient %s, probability %.1f%%, model %s",
                recipientName, assessment.getId(), assessment.getPatientId(),
                assessment.getProbability() * 100.0, assessment.getModelName()));
    }

    /** @return number of alerts raised so far */
    public int getAlertCount() {
        return alertCount.get();
    }
}
