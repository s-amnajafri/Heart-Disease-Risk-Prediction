# Viva cheat-sheet - Heart Disease Risk Prediction System

Short answers you can say in 20-30 seconds. Class and method names are the real ones from the code.

## OOAD concepts

**1. Where did you use inheritance?**
`User` is an abstract class with `id`, `name`, `email` and the abstract method `getRole()`. `Patient` and `Doctor` extend it and
implement `getRole()` ("PATIENT" / "DOCTOR"), so the shared data and validation are written once in `User`.

**2. What is the difference between composition and aggregation, and where are they in your code?**
Composition = strong "owns" relationship: a `Patient` creates its `HealthRecord` inside the constructor from a
`HealthRecord.Builder`, nobody else holds it, and it disappears with the patient. Aggregation = weak "has" relationship: a
Each `Doctor` holds a `List<Patient>`, but patients are created independently and stay alive when removed from a doctor's list (`removePatient`). The intake form lets a patient choose one of the configured doctors; the selected doctor ID is saved with the patient.

**3. How did you apply encapsulation?**
All fields are `private final`; objects are read through getters only. `HealthRecord` can only be created through
`HealthRecord.Builder.build()`, which validates all 13 ranges (age 1-120, chol 100-600, ...) and throws
`IllegalArgumentException`, so an invalid record can never exist in the system. The API turns that exception into HTTP 400.

**4. Abstract class or interface? Where is polymorphism?**
Use an abstract class when subclasses share state and code (`User`); use an interface when unrelated classes only share a
contract (`PredictionModel`, `AssessmentListener`, `PatientRepository`). Polymorphism: `PredictionService` calls
`model.predictProbability(record)` on a `PredictionModel` reference and the logistic or the rule-based implementation runs,
depending on the object at runtime.

**5. Why did you split the system into a Java backend and a Streamlit frontend?**
Separation of concerns: the backend holds all logic (OOAD, ML, validation, storage) and exposes a REST API; the frontend only
shows inputs and results and calls the API with `requests`. Either side can be replaced without touching the other, and the
backend can be tested without a UI (`curl`, JUnit).

## Design patterns

**6. Explain the Strategy pattern in your project.**
`PredictionModel` is the strategy interface, `LogisticRegressionModel` and `RuleBasedModel` are concrete strategies, and
`PredictionService` is the context that holds one model. `setModel()` swaps the algorithm at runtime; `assessWith(model, patient)`
does the swap and the assessment in one `synchronized` step so concurrent requests cannot mix up models.

**7. Why a Factory?**
`ModelFactory.create("logistic" | "rule")` hides which concrete class is built and reads the hyper-parameters from
`ConfigManager`. Callers use a string key from the REST query parameter and never write `new LogisticRegressionModel(...)`;
adding a third model means one new `case`. Unknown keys throw `IllegalArgumentException` (HTTP 400).

**8. How is your Singleton thread-safe?**
`ConfigManager` uses the initialization-on-demand holder idiom: the instance lives in a private static nested `Holder` class, which
the JVM initialises exactly once and thread-safely when `getInstance()` is first called. It is lazy, needs no `synchronized`
or double-checked locking, and the fields are `final`, so the shared object is immutable.

**9. Why a Builder for `HealthRecord`?**
It has 13 fields; a 13-argument constructor is error-prone (many `int`s in a row). `HealthRecord.builder().age(63).sex(1)...build()`
is readable, and `build()` is the single place where validation happens. The result is an immutable object.

**10. How does the Observer pattern work here?**
`PredictionService` is the subject: it keeps a `List<AssessmentListener>` and calls `onAssessmentCreated(assessment)` after storing
each assessment. `HighRiskNotifier` is the concrete observer; it checks `level == HIGH`, looks up the patient's assigned doctor and logs an alert for that doctor. New reactions (e-mail, SMS) can be added as new listeners without changing `PredictionService`.

**11. What is the Repository pattern and why did you use it?**
`PatientRepository` is an interface for storing and finding patients and assessments; `CsvPatientRepository` implements it with
`patients.csv` and `assessments.csv`. Services only know the interface, so the CSV files could be replaced by a database without
changing any service class, and tests can use a temporary folder.

**12. How are doctors assigned, and is there authentication?**
The system currently provides a small configured directory of doctors. The patient selects one in the intake form; `PatientService` validates the doctor ID, stores it in the `Patient`, and `CsvPatientRepository` persists it in `patients.csv`. There is no login/authentication workflow because this project focuses on risk assessment and OOAD rather than user accounts.

**13. How does the History page work now?**
`GET /api/patients` returns all registered patients and their assessments. The UI displays the complete list by default, filters it by name, email or patient ID, and lets the user expand a patient to view every assessment. The individual history endpoint remains available too.

## Machine learning

**14. How does your logistic regression work?**
For a patient it computes z = b + sum(w_j * x_j) on the standardised features and returns p = 1 / (1 + e^-z), a probability
between 0 and 1. Training uses batch gradient descent on the log-loss: in each of 2000 epochs it computes the error (p - y) for all
820 training rows, averages the gradient and moves the weights by learning rate 0.1 against it. Everything is written by hand
in `LogisticRegressionModel`; no ML library is used.

**15. Why standardise the features and why store the means and standard deviations?**
Features have very different scales (chol around 250, oldpeak around 1), which makes gradient descent slow or unstable. The
z-score (x - mean) / std puts them on the same scale. The means and standard deviations come from the training data only and are
stored, so a new patient is scaled exactly like the training rows and there is no leakage from the test split.

**16. What do accuracy, precision, recall, F1 and the confusion matrix mean? Which matters most here?**
Accuracy = correct / all. Precision = true positives / predicted positives. Recall = true positives / actual positives. F1 =
harmonic mean of precision and recall. The confusion matrix shows TP, FP, TN, FN (`ModelEvaluator`, threshold 0.5, positive =
disease present). In a screening tool a false negative (missed disease) is worse than a false positive, so recall is the most
important number to watch.

**17. What are the limitations of your system?**
It is an educational tool, not a diagnosis. The CSV has 1,025 rows but only 302 distinct ones, so many test rows also occur in the
training split and the reported metrics are optimistic. In this file `target = 0` means disease present (handled by
`DatasetLoader`), the data comes from one historical study, and the rule-based thresholds are simple textbook-style values.
Future work: remove duplicates before splitting, cross-validation, more models through the same Strategy/Factory.
