package com.heartrisk;

import com.heartrisk.api.ApiHandlers;
import com.heartrisk.api.ApiServer;
import com.heartrisk.config.ConfigManager;
import com.heartrisk.data.CsvPatientRepository;
import com.heartrisk.data.DatasetLoader;
import com.heartrisk.data.LabelledDataset;
import com.heartrisk.data.PatientRepository;
import com.heartrisk.model.Doctor;
import com.heartrisk.observer.HighRiskNotifier;
import com.heartrisk.prediction.EvaluationResult;
import com.heartrisk.service.ModelCatalog;
import com.heartrisk.service.PatientService;
import com.heartrisk.service.PredictionService;
import com.heartrisk.service.RecommendationService;
import com.heartrisk.service.ReportService;
import java.io.IOException;
import java.util.List;

/** Application entry point: loads data, trains the models once, wires the objects and starts the REST API. */
public final class Main {

    private Main() {
    }

    /**
     * Starts the system.
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        ConfigManager config = ConfigManager.getInstance();
        try {
            LabelledDataset dataset = new DatasetLoader().load(config.getDatasetPath());
            System.out.printf("Loaded %d rows from %s (%d skipped for missing values)%n",
                    dataset.size(), config.getDatasetPath(), dataset.skippedRows());

            ModelCatalog catalog = ModelCatalog.trainFrom(dataset, config);
            EvaluationResult logistic = catalog.getMetrics("logistic");
            System.out.printf("Trained models on %d rows, tested on %d rows. Logistic accuracy: %.3f%n",
                    catalog.getTrainSize(), catalog.getTestSize(), logistic.accuracy());

            PatientRepository repository = new CsvPatientRepository(config.getDataDirectory());
            List<Doctor> doctors = List.of(
                    new Doctor("D-001", "Dr. Ayesha Khan", "ayesha.khan@heartrisk.local", "Cardiology"),
                    new Doctor("D-002", "Dr. Hamza Ali", "hamza.ali@heartrisk.local", "Internal Medicine"),
                    new Doctor("D-003", "Dr. Sara Ahmed", "sara.ahmed@heartrisk.local", "Cardiology"),
                    new Doctor("D-004", "Dr. Usman Raza", "usman.raza@heartrisk.local", "General Medicine"));

            PredictionService predictions = new PredictionService(catalog.get("logistic"), repository);
            // High-risk notifications are logged by the observer; patient assignment is persisted separately.
            predictions.addListener(new HighRiskNotifier(repository, doctors));
            RecommendationService recommendations = new RecommendationService();
            ApiHandlers handlers = new ApiHandlers(new PatientService(repository, doctors), predictions,
                    catalog, recommendations, new ReportService(recommendations));

            new ApiServer(handlers).start(config.getPort());
            System.out.println("Heart Risk API running on http://localhost:" + config.getPort());
        } catch (IOException e) {
            System.err.println("Startup failed: " + e.getMessage());
            System.exit(1);
        }
    }
}
