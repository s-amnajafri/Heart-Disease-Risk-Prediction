# Modelio build sheets - Heart Disease Risk Prediction System

Eight UML diagrams, each described so you can rebuild it in Modelio element by element. Every class, method,
attribute and object name below is taken from the Java code in `backend/src/main/java/com/heartrisk/`.

**Conventions used in the sheets**

* Visibility: `+` public, `-` private, `#` protected. `{abstract}`, `{static}` as UML properties.
* `[x]` in a relationship table is a multiplicity. `«x»` is a stereotype.
* Palette tool names (Actor, Use case, Include, Extend, Association, Aggregation, Composition, Generalization,
  Interface realization, Dependency, Lifeline, Message, ...) are the standard UML terms; the exact labels differ a little
  between Modelio versions, so hover over the palette icons if a name is not found. Aggregation/composition can also be set
  on an association end (property "Aggregation"). Stereotype names are typed in the element's *Properties* view.
* Lines marked **Report text** and **Figure caption** are ready to paste into the report (they are also used in the .docx).

**One-time Modelio setup**

1. File > New project, name it `HeartRiskSystem`.
2. In the model browser create one UML *Package* per diagram family, for example `Analysis` (use case, activity),
   `Design` (class, object, sequence, communication) and `Architecture` (component, deployment).
3. Create each diagram with: right-click the package > *Create diagram* > choose the diagram type > give it the name
   written in this sheet. Export each as PNG (right-click the diagram > *Export diagram*) for the report.

---

## 1. Use Case Diagram

**Diagram name:** `UC - Heart Disease Risk Prediction`
**Code evidence:** REST endpoints in `ApiServer`; handlers in `ApiHandlers`; alert in `HighRiskNotifier`.

### 1.1 Elements to create

| Type | Exact name | Notes |
|---|---|---|
| System boundary | `Heart Disease Risk Prediction System` | Create a *Class* with this name and drop the use cases inside it (or draw it as a Package/rectangle) |
| Actor | `Patient` | Person who is assessed |
| Actor | `Doctor` | Reviews patients; receives alerts |
| Use case | `Register Patient` | `POST /api/patients` -> `PatientService.register` |
| Use case | `Predict Risk` | `POST /api/predict` -> `PredictionService.assessWith` |
| Use case | `Validate Health Record` | `HealthRecord.Builder.build()` |
| Use case | `Select Prediction Model` | `?model=logistic\|rule` -> `ModelCatalog.get` |
| Use case | `Generate Recommendations` | `RecommendationService.recommend` |
| Use case | `Receive High-Risk Alert` | `HighRiskNotifier.onAssessmentCreated` |
| Use case | `View Assessment History` | `GET /api/patients/{id}/history` |
| Use case | `Download Risk Report` | `GET /api/report/{assessmentId}` |
| Use case | `View Model Metrics` | `GET /api/model/metrics` |

### 1.2 Relationships

| From | To | UML type | Label / condition |
|---|---|---|---|
| `Patient` | `Register Patient` | Association | |
| `Patient` | `Predict Risk` | Association | |
| `Patient` | `View Assessment History` | Association | |
| `Patient` | `Download Risk Report` | Association | |
| `Doctor` | `Register Patient` | Association | |
| `Doctor` | `Predict Risk` | Association | |
| `Doctor` | `View Assessment History` | Association | |
| `Doctor` | `Download Risk Report` | Association | |
| `Doctor` | `View Model Metrics` | Association | |
| `Doctor` | `Receive High-Risk Alert` | Association | |
| `Predict Risk` | `Validate Health Record` | Include (`«include»`) | always happens |
| `Predict Risk` | `Select Prediction Model` | Include (`«include»`) | always happens |
| `Predict Risk` | `Generate Recommendations` | Include (`«include»`) | always happens |
| `Receive High-Risk Alert` | `Predict Risk` | Extend (`«extend»`) | extension point `after assessment is stored`; condition `level == HIGH (probability >= 0.66)` |
| `Download Risk Report` | `Predict Risk` | Extend (`«extend»`) | condition `user requests the report` |

Direction rule: the arrow of `«include»` and `«extend»` is a dashed arrow; `«include»` points from the base use case to the
included one, `«extend»` points from the extending use case to the base use case.

### 1.3 Build steps

1. Create the diagram and drag/draw the system boundary class.
2. Place the two actors outside the boundary (Patient left, Doctor right).
3. Create the 9 use cases inside the boundary with the exact names above.
4. Draw the 10 associations, then the 3 includes, then the 2 extends. Type the extension condition in the
   extend relationship's properties (*Condition* / note).

> **Report text:** The use case diagram shows two actors, Patient and Doctor, interacting with the Heart Disease Risk Prediction System. The central use case Predict Risk always includes validating the health record, selecting a prediction model and generating recommendations, and it is extended by the optional report download and by the high-risk alert that the Doctor receives when the predicted level is HIGH.
> **Figure caption:** Figure 1: Use case diagram of the Heart Disease Risk Prediction System.

---

## 2. Activity Diagram

**Diagram name:** `AD - Predict Risk`
**Scenario:** user clicks *Predict* in the Streamlit app (register patient if needed, then predict).
**Code evidence:** `frontend/app.py` (`predict_page`), `ApiHandlers.createPatient/predict`, `PredictionService.assessWith`.

### 2.1 Partitions (swimlanes) - create three

`User` | `Streamlit Frontend` | `Java REST API`

### 2.2 Nodes

| # | Node type | Exact name | Partition |
|---|---|---|---|
| 1 | Initial node | (filled circle) | User |
| 2 | Action | `Enter patient details and 13 health features` | User |
| 3 | Action | `Select prediction model` | User |
| 4 | Action | `Click Predict` | User |
| 5 | Decision | `Same inputs as previous prediction?` | Streamlit Frontend |
| 6 | Action | `Reuse stored patient id` | Streamlit Frontend |
| 7 | Action | `Send POST /api/patients` | Streamlit Frontend |
| 8 | Action | `Validate request and build HealthRecord` | Java REST API |
| 9 | Decision | `Data valid?` | Java REST API |
| 10 | Action | `Create Patient and save in repository` | Java REST API |
| 11 | Action | `Return patient id (HTTP 201)` | Java REST API |
| 12 | Merge | (diamond, no text) | Streamlit Frontend |
| 13 | Action | `Send POST /api/predict with model` | Streamlit Frontend |
| 14 | Action | `Look up model in ModelCatalog` | Java REST API |
| 15 | Decision | `Model and patient found?` | Java REST API |
| 16 | Action | `Calculate probability with selected PredictionModel` | Java REST API |
| 17 | Action | `Create and save RiskAssessment (level from RiskLevel)` | Java REST API |
| 18 | Decision | `Level is HIGH?` | Java REST API |
| 19 | Action | `HighRiskNotifier logs alert` | Java REST API |
| 20 | Merge | (diamond, no text) | Java REST API |
| 21 | Action | `Build recommendations` | Java REST API |
| 22 | Action | `Return probability, level, model and recommendations` | Java REST API |
| 23 | Action | `Show probability bar, colour-coded level and recommendations` | Streamlit Frontend |
| 24 | Action | `Offer report download` | Streamlit Frontend |
| 25 | Activity final node | (circle with ring) | Streamlit Frontend |
| 26 | Action | `Return JSON error (HTTP 400 or 404)` | Java REST API |
| 27 | Action | `Show error message` | Streamlit Frontend |
| 28 | Flow final node | (circle with X) | Streamlit Frontend |

### 2.3 Control flows (in order)

1 -> 2 -> 3 -> 4 -> 5.
From **5**: `[yes]` -> 6 -> 12; `[no]` -> 7 -> 8 -> 9.
From **9**: `[yes]` -> 10 -> 11 -> 12; `[no]` -> 26.
12 -> 13 -> 14 -> 15.
From **15**: `[yes]` -> 16; `[no]` -> 26.
16 -> 17 -> 18.
From **18**: `[yes]` -> 19 -> 20; `[no]` -> 20.
20 -> 21 -> 22 -> 23 -> 24 -> 25.
26 -> 27 -> 28.

Put guard text in square brackets on the flows leaving each decision. Node 12 and node 20 are *merge* nodes (several flows in, one out).

### 2.4 Build steps

1. Create the diagram, add the three partitions side by side, name them.
2. Add nodes 1-28 inside the correct partition; name actions exactly as above.
3. Draw the control flows in the order of 2.3; add guards to the six decision outputs.
4. Check there is exactly one initial node and every path ends in node 25 or node 28.

> **Report text:** The activity diagram follows the Predict Risk scenario across three swimlanes: User, Streamlit Frontend and Java REST API. The frontend registers the patient (or reuses the stored id), the API validates the data, calculates the probability with the selected model, stores the assessment, lets the observer raise an alert for HIGH risk and returns recommendations; invalid input or an unknown model ends in an error message.
> **Figure caption:** Figure 2: Activity diagram of the Predict Risk scenario.

---

## 3. Class Diagram

**Diagram name:** `CD - Heart Risk System`
Create seven Modelio *Packages* inside `Design` and put each class in its package: `model`, `prediction`, `data`, `service`,
`observer`, `config`, `api`. Use the exact names. Private helper methods and constants that are not listed
below (for example `sigmoid`, `CsvUtil`, `FeatureValidator`, `RequestParser`, `ResponseMapper`, `NotFoundException`, `Main`)
are implementation details and are deliberately left out of the diagram; `Main` only wires the objects together.

### 3.1 Classes (package `model`)

**User** `«abstract»`
- Attributes: `-id : String`, `-name : String`, `-email : String`
- Methods: `#User(id : String, name : String, email : String)`, `+getId() : String`, `+getName() : String`, `+getEmail() : String`, `+getRole() : String {abstract}`

**Patient**
- Attributes: `-healthRecord : HealthRecord`
- Methods: `+Patient(id : String, name : String, email : String, recordBuilder : HealthRecord.Builder)`, `+getHealthRecord() : HealthRecord`, `+getRole() : String`

**Doctor**
- Attributes: `-specialty : String`, `-patients : List<Patient>`
- Methods: `+Doctor(id : String, name : String, email : String, specialty : String)`, `+getSpecialty() : String`, `+addPatient(patient : Patient) : void`, `+removePatient(patient : Patient) : boolean`, `+getPatients() : List<Patient>`, `+getRole() : String`

**HealthRecord** `«final»`
- Attributes: `-age : int`, `-sex : int`, `-cp : int`, `-trestbps : int`, `-chol : int`, `-fbs : int`, `-restecg : int`, `-thalach : int`, `-exang : int`, `-oldpeak : double`, `-slope : int`, `-ca : int`, `-thal : int`, `+FEATURE_NAMES : List<String> {static, readOnly}`
- Methods: `+builder() : HealthRecord.Builder {static}`, `+getAge() : int`, `+getSex() : int`, `+getCp() : int`, `+getTrestbps() : int`, `+getChol() : int`, `+getFbs() : int`, `+getRestecg() : int`, `+getThalach() : int`, `+getExang() : int`, `+getOldpeak() : double`, `+getSlope() : int`, `+getCa() : int`, `+getThal() : int`, `+toFeatureVector() : double[]`, `+toMap() : Map<String, Object>`

**HealthRecord.Builder** `«Builder»` (nested class of HealthRecord)
- Attributes: `-age : Integer`, `-sex : Integer`, `-cp : Integer`, `-trestbps : Integer`, `-chol : Integer`, `-fbs : Integer`, `-restecg : Integer`, `-thalach : Integer`, `-exang : Integer`, `-oldpeak : Double`, `-slope : Integer`, `-ca : Integer`, `-thal : Integer`
- Methods: `+age(v : int) : Builder`, `+sex(v : int) : Builder`, `+cp(v : int) : Builder`, `+trestbps(v : int) : Builder`, `+chol(v : int) : Builder`, `+fbs(v : int) : Builder`, `+restecg(v : int) : Builder`, `+thalach(v : int) : Builder`, `+exang(v : int) : Builder`, `+oldpeak(v : double) : Builder`, `+slope(v : int) : Builder`, `+ca(v : int) : Builder`, `+thal(v : int) : Builder`, `+feature(name : String, value : double) : Builder`, `+build() : HealthRecord`

**RiskLevel** `«enumeration»`
- Literals: `LOW`, `MEDIUM`, `HIGH`
- Methods: `+fromProbability(probability : double) : RiskLevel {static}`

**RiskAssessment**
- Attributes: `-id : String`, `-patientId : String`, `-probability : double`, `-level : RiskLevel`, `-modelName : String`, `-timestamp : Instant`
- Methods: `+RiskAssessment(id : String, patientId : String, probability : double, modelName : String, timestamp : Instant)`, `+getId() : String`, `+getPatientId() : String`, `+getProbability() : double`, `+getLevel() : RiskLevel`, `+getModelName() : String`, `+getTimestamp() : Instant`

### 3.2 Classes (package `prediction`)

**PredictionModel** `«interface»`
- Methods: `+predictProbability(record : HealthRecord) : double`, `+getName() : String`

**Trainable** `«interface»`
- Methods: `+train(trainingData : LabelledDataset) : void`

**LogisticRegressionModel**
- Attributes: `-learningRate : double`, `-epochs : int`, `-means : double[]`, `-stdDevs : double[]`, `-weights : double[]`, `-bias : double`, `-trained : boolean`
- Methods: `+LogisticRegressionModel(learningRate : double, epochs : int)`, `+train(trainingData : LabelledDataset) : void`, `+predictProbability(record : HealthRecord) : double`, `+getName() : String`

**RuleBasedModel**
- Methods: `+RuleBasedModel()`, `+predictProbability(record : HealthRecord) : double`, `+getName() : String`

**ModelFactory** `«Factory»`
- Attributes: `+SUPPORTED_KEYS : List<String> {static, readOnly}`
- Methods: `+create(key : String) : PredictionModel {static}`

**ModelEvaluator**
- Attributes: `+DECISION_THRESHOLD : double {static, readOnly}`
- Methods: `+evaluate(model : PredictionModel, test : LabelledDataset) : EvaluationResult`

**EvaluationResult** `«record»`
- Attributes: `-truePositives : int`, `-falsePositives : int`, `-trueNegatives : int`, `-falseNegatives : int`
- Methods: `+total() : int`, `+accuracy() : double`, `+precision() : double`, `+recall() : double`, `+f1() : double`

### 3.3 Classes (package `data`)

**LabelledDataset** `«record»`
- Attributes: `-records : List<HealthRecord>`, `-labels : int[]`, `-skippedRows : int`
- Methods: `+size() : int`, `+split(trainFraction : double, seed : long) : Split`

**DatasetLoader**
- Methods: `+DatasetLoader()`, `+load(path : Path) : LabelledDataset`

**PatientRepository** `«interface»` `«Repository»`
- Methods: `+save(patient : Patient) : void`, `+findById(id : String) : Optional<Patient>`, `+findAll() : List<Patient>`, `+saveAssessment(assessment : RiskAssessment) : void`, `+findAssessmentById(id : String) : Optional<RiskAssessment>`, `+findAssessmentsByPatient(patientId : String) : List<RiskAssessment>`, `+nextPatientId() : String`, `+nextAssessmentId() : String`

**CsvPatientRepository**
- Attributes: `-patientsFile : Path`, `-assessmentsFile : Path`, `-patients : Map<String, Patient>`, `-assessments : List<RiskAssessment>`, `-patientCounter : int`, `-assessmentCounter : int`
- Methods: `+CsvPatientRepository(dataDirectory : Path)`, `+save(patient : Patient) : void`, `+findById(id : String) : Optional<Patient>`, `+findAll() : List<Patient>`, `+saveAssessment(assessment : RiskAssessment) : void`, `+findAssessmentById(id : String) : Optional<RiskAssessment>`, `+findAssessmentsByPatient(patientId : String) : List<RiskAssessment>`, `+nextPatientId() : String`, `+nextAssessmentId() : String`

### 3.4 Classes (package `service`)

**PredictionService** `«Strategy context»` `«Observer subject»`
- Attributes: `-repository : PatientRepository`, `-listeners : List<AssessmentListener>`, `-model : PredictionModel`
- Methods: `+PredictionService(initialModel : PredictionModel, repository : PatientRepository)`, `+setModel(newModel : PredictionModel) : void`, `+getModel() : PredictionModel`, `+addListener(listener : AssessmentListener) : void`, `+assess(patient : Patient) : RiskAssessment`, `+assessWith(newModel : PredictionModel, patient : Patient) : RiskAssessment`

**PatientService**
- Attributes: `-repository : PatientRepository`, `-doctors : Map<String, Doctor>`
- Methods: `+PatientService(repository : PatientRepository, doctors : List<Doctor>)`, `+getDoctors() : List<Doctor>`, `+register(name : String, email : String, builder : HealthRecord.Builder, doctorId : String) : Patient`, `+getAllPatients() : List<Patient>`, `+getPatient(id : String) : Patient`, `+getAssessment(id : String) : RiskAssessment`, `+getHistory(patientId : String) : List<RiskAssessment>`

**ModelCatalog**
- Attributes: `-models : Map<String, PredictionModel>`, `-metrics : Map<String, EvaluationResult>`, `-trainSize : int`, `-testSize : int`
- Methods: `+trainFrom(dataset : LabelledDataset, config : ConfigManager) : ModelCatalog {static}`, `+get(key : String) : PredictionModel`, `+getMetrics(key : String) : EvaluationResult`, `+getTrainSize() : int`, `+getTestSize() : int`

**RecommendationService**
- Attributes: `+DISCLAIMER : String {static, readOnly}`
- Methods: `+RecommendationService()`, `+recommend(level : RiskLevel, record : HealthRecord) : List<String>`

**ReportService**
- Attributes: `-recommendations : RecommendationService`
- Methods: `+ReportService(recommendations : RecommendationService)`, `+generate(assessment : RiskAssessment, patient : Patient) : String`

### 3.5 Classes (packages `observer`, `config`, `api`)

**AssessmentListener** `«interface»` `«Observer»`
- Methods: `+onAssessmentCreated(assessment : RiskAssessment) : void`

**HighRiskNotifier**
- Attributes: `-doctor : Doctor`, `-alertCount : AtomicInteger`
- Methods: `+HighRiskNotifier(doctor : Doctor)`, `+onAssessmentCreated(assessment : RiskAssessment) : void`, `+getAlertCount() : int`

**ConfigManager** `«Singleton»`
- Attributes: `-port : int`, `-learningRate : double`, `-epochs : int`, `-splitSeed : long`, `-datasetPath : Path`
- Methods: `-ConfigManager()`, `+getInstance() : ConfigManager {static}`, `+getPort() : int`, `+getDatasetPath() : Path`, `+getDataDirectory() : Path`, `+getLearningRate() : double`, `+getEpochs() : int`, `+getSplitSeed() : long`

**ApiServer**
- Attributes: `-app : Javalin`
- Methods: `+ApiServer(handlers : ApiHandlers)`, `+start(port : int) : ApiServer`, `+stop() : void`

**ApiHandlers**
- Attributes: `-patients : PatientService`, `-predictions : PredictionService`, `-catalog : ModelCatalog`, `-recommendations : RecommendationService`, `-reports : ReportService`
- Methods: `+ApiHandlers(patients : PatientService, predictions : PredictionService, catalog : ModelCatalog, recommendations : RecommendationService, reports : ReportService)`, `+health(ctx : Context) : void`, `+createPatient(ctx : Context) : void`, `+predict(ctx : Context) : void`, `+history(ctx : Context) : void`, `+metrics(ctx : Context) : void`, `+report(ctx : Context) : void`

### 3.6 Relationships

| # | From | To | UML type | Multiplicity (from -> to) | Role / note |
|---|---|---|---|---|---|
| 1 | `Patient` | `User` | Generalization (inheritance) | | Patient is a User |
| 2 | `Doctor` | `User` | Generalization (inheritance) | | Doctor is a User |
| 3 | `Patient` | `HealthRecord` | **Composition** (filled diamond at Patient) | `1` -> `1` | role `healthRecord`; created inside the constructor, destroyed with the patient |
| 4 | `Doctor` | `Patient` | **Aggregation** (hollow diamond at Doctor) | `1` -> `0..*` | role `patients`; patients exist independently |
| 5 | `HealthRecord` | `HealthRecord.Builder` | Nested class (containment `+--`) | | Builder pattern |
| 6 | `HealthRecord.Builder` | `HealthRecord` | Dependency `«create»` | | `build()` |
| 7 | `Patient` | `HealthRecord.Builder` | Dependency `«use»` | | constructor parameter |
| 8 | `LogisticRegressionModel` | `PredictionModel` | Interface realization | | |
| 9 | `RuleBasedModel` | `PredictionModel` | Interface realization | | |
| 10 | `LogisticRegressionModel` | `Trainable` | Interface realization | | |
| 11 | `CsvPatientRepository` | `PatientRepository` | Interface realization | | |
| 12 | `HighRiskNotifier` | `AssessmentListener` | Interface realization | | |
| 13 | `RiskAssessment` | `RiskLevel` | Association (navigable to RiskLevel) | `1` -> `1` | role `level` |
| 14 | `RiskAssessment` | `Patient` | Association (navigable to Patient) | `0..*` -> `1` | stored as `patientId : String` (no object reference) |
| 15 | `PredictionService` | `PredictionModel` | Association (navigable) | `1` -> `1` | role `model` - **Strategy** |
| 16 | `PredictionService` | `PatientRepository` | Association (navigable) | `1` -> `1` | role `repository` |
| 17 | `PredictionService` | `AssessmentListener` | Association (navigable) | `1` -> `0..*` | role `listeners` - **Observer** |
| 18 | `PredictionService` | `RiskAssessment` | Dependency `«create»` | | `assess()` |
| 19 | `HighRiskNotifier` | `Doctor` | Association (navigable) | `1` -> `1` | role `doctor` |
| 20 | `PatientService` | `PatientRepository` | Association (navigable) | `1` -> `1` | role `repository` |
| 21 | `PatientService` | `Doctor` | Association (navigable) | `1` -> `1` | role `doctor` |
| 22 | `PatientService` | `Patient` | Dependency `«create»` | | `register()` |
| 23 | `CsvPatientRepository` | `Patient` | Association (navigable) | `1` -> `0..*` | role `patients` |
| 24 | `CsvPatientRepository` | `RiskAssessment` | Association (navigable) | `1` -> `0..*` | role `assessments` |
| 25 | `ModelCatalog` | `PredictionModel` | Association (navigable) | `1` -> `1..*` | role `models` |
| 26 | `ModelCatalog` | `EvaluationResult` | Association (navigable) | `1` -> `1..*` | role `metrics` |
| 27 | `ModelCatalog` | `ModelFactory` | Dependency `«use»` | | creates the models |
| 28 | `ModelCatalog` | `ModelEvaluator` | Dependency `«use»` | | evaluates the models |
| 29 | `ModelCatalog` | `Trainable` | Dependency `«use»` | | trains `Trainable` models |
| 30 | `ModelCatalog` | `ConfigManager` | Dependency `«use»` | | split seed |
| 31 | `ModelFactory` | `PredictionModel` | Dependency `«create»` | | **Factory** |
| 32 | `ModelFactory` | `LogisticRegressionModel` | Dependency `«instantiate»` | | |
| 33 | `ModelFactory` | `RuleBasedModel` | Dependency `«instantiate»` | | |
| 34 | `ModelFactory` | `ConfigManager` | Dependency `«use»` | | hyper-parameters |
| 35 | `ModelEvaluator` | `PredictionModel` | Dependency `«use»` | | |
| 36 | `ModelEvaluator` | `EvaluationResult` | Dependency `«create»` | | |
| 37 | `ModelEvaluator` | `LabelledDataset` | Dependency `«use»` | | |
| 38 | `Trainable` | `LabelledDataset` | Dependency `«use»` | | `train()` parameter |
| 39 | `LabelledDataset` | `HealthRecord` | Association (navigable) | `1` -> `0..*` | role `records` |
| 40 | `DatasetLoader` | `LabelledDataset` | Dependency `«create»` | | `load()` |
| 41 | `RecommendationService` | `RiskLevel` | Dependency `«use»` | | |
| 42 | `RecommendationService` | `HealthRecord` | Dependency `«use»` | | |
| 43 | `ReportService` | `RecommendationService` | Association (navigable) | `1` -> `1` | role `recommendations` |
| 44 | `ReportService` | `RiskAssessment` | Dependency `«use»` | | `generate()` |
| 45 | `ApiServer` | `ApiHandlers` | Dependency `«use»` | | routes call the handlers |
| 46 | `ApiHandlers` | `PatientService` | Association (navigable) | `1` -> `1` | role `patients` |
| 47 | `ApiHandlers` | `PredictionService` | Association (navigable) | `1` -> `1` | role `predictions` |
| 48 | `ApiHandlers` | `ModelCatalog` | Association (navigable) | `1` -> `1` | role `catalog` |
| 49 | `ApiHandlers` | `RecommendationService` | Association (navigable) | `1` -> `1` | role `recommendations` |
| 50 | `ApiHandlers` | `ReportService` | Association (navigable) | `1` -> `1` | role `reports` |

### 3.7 Build steps

1. Create the seven packages, then in each one create its classes/interfaces/enumeration/records with the *Class*, *Interface*,
   *Enumeration* tools; set the stereotype for abstract/record/pattern labels in the Properties view.
2. For every class add the attributes and operations of 3.1-3.5 (type and visibility exactly as written).
3. Draw relationships 1-50. Do the structural ones first (1-5, 8-17, 19-21, 23-26, 39, 43, 46-50), dependencies last.
4. Show only the compartments you need if the diagram is crowded (hide attributes of the service classes). Arrange packages as
   `api` on top, `service` below, then `prediction`/`data`/`observer`, `model` and `config` at the bottom.

> **Report text:** The class diagram shows the whole design. User is an abstract superclass of Patient and Doctor; each Patient is composed of exactly one HealthRecord, while a Doctor merely aggregates a list of independent Patients. The PredictionModel interface is realised by LogisticRegressionModel and RuleBasedModel and is used by PredictionService (Strategy), created by ModelFactory (Factory), while ConfigManager (Singleton), HealthRecord.Builder (Builder), AssessmentListener (Observer) and PatientRepository (Repository) complete the six design patterns.
> **Figure caption:** Figure 3: Class diagram with inheritance, composition, aggregation, interfaces and design patterns.

---

## 4. Object Diagram

**Diagram name:** `OD - After one Predict session`
**Snapshot:** one patient registered through the Streamlit app and predicted twice (logistic, then rule) while the dataset has
1,025 rows. Use your own run's values where noted. Create each object with the *Instance* tool, set *Name* and *Base class*
(its class), and add one *attribute link* (slot) per row below.

### 4.1 Objects and slot values

| Object (name : Class) | Slots |
|---|---|
| `doctor1 : Doctor` | `id = "D-001"`, `name = "Dr. Ayesha Khan"`, `email = "ayesha.khan@heartrisk.local"`, `specialty = "Cardiology"` |
| `doctor2 : Doctor` | `id = "D-002"`, `name = "Dr. Hamza Ali"`, `email = "hamza.ali@heartrisk.local"`, `specialty = "Internal Medicine"` |
| `doctor3 : Doctor` | `id = "D-003"`, `name = "Dr. Sara Ahmed"`, `email = "sara.ahmed@heartrisk.local"`, `specialty = "Cardiology"` |
| `doctor4 : Doctor` | `id = "D-004"`, `name = "Dr. Usman Raza"`, `email = "usman.raza@heartrisk.local"`, `specialty = "General Medicine"` |
| `p1 : Patient` | `id = "P-0001"`, `name = "Demo Patient"`, `email = "demo@example.com"` |
| `rec1 : HealthRecord` | `age = 63`, `sex = 1`, `cp = 0`, `trestbps = 150`, `chol = 290`, `fbs = 1`, `restecg = 1`, `thalach = 105`, `exang = 1`, `oldpeak = 3.2`, `slope = 0`, `ca = 2`, `thal = 3` |
| `a1 : RiskAssessment` | `id = "A-0001"`, `patientId = "P-0001"`, `probability = 0.999` (copy your value), `level = HIGH`, `modelName = "Logistic Regression"`, `timestamp = <time of your run>` |
| `a2 : RiskAssessment` | `id = "A-0002"`, `patientId = "P-0001"`, `probability = 1.0`, `level = HIGH`, `modelName = "Rule-Based Score"`, `timestamp = <time of your run>` |
| `logisticModel : LogisticRegressionModel` | `learningRate = 0.1`, `epochs = 2000`, `trained = true` |
| `ruleModel : RuleBasedModel` | (no slots) |
| `service : PredictionService` | (no slots; links below) |
| `notifier : HighRiskNotifier` | `alertCount = 2` |
| `repo : CsvPatientRepository` | `patientCounter = 1`, `assessmentCounter = 2` |
| `catalog : ModelCatalog` | `trainSize = 820`, `testSize = 205` |
| `config : ConfigManager` | `port = 7070`, `learningRate = 0.1`, `epochs = 2000`, `splitSeed = 42`, `datasetPath = "../data/heart.csv"` |

The numbers 820 and 205 are 80 % and 20 % of 1,025 rows. `probability` of `a1` depends on the trained model; read it from the
Predict page or `GET /api/model/metrics` run, and keep it identical in the report. `service` holds `ruleModel` because the last
prediction used the rule-based model (`assessWith` swapped the strategy).

### 4.2 Links (instances of the class-diagram relationships)

| From | To | Link type | Role (association end) |
|---|---|---|---|
| `duty` | `p1` | Aggregation link | `patients` |
| `p1` | `rec1` | Composition link | `healthRecord` |
| `a1` | `p1` | Link | `patientId` (value "P-0001") |
| `a2` | `p1` | Link | `patientId` (value "P-0001") |
| `service` | `ruleModel` | Link | `model` |
| `service` | `repo` | Link | `repository` |
| `service` | `notifier` | Link | `listeners` |
| `notifier` | `duty` | Link | `doctor` |
| `catalog` | `logisticModel` | Link | `models` |
| `catalog` | `ruleModel` | Link | `models` |
| `repo` | `p1` | Link | `patients` |
| `repo` | `a1` | Link | `assessments` |
| `repo` | `a2` | Link | `assessments` |

### 4.3 Build steps

1. Create the 12 instances and set base classes. 2. Add the slots (attribute links). 3. Draw the 13 links with the roles above.
4. Put `duty`, `p1`, `rec1` on the left, `a1`, `a2` in the middle and the services on the right.

> **Report text:** The object diagram is a snapshot of the running system after one patient, p1, was registered and assessed with both models. The doctor object duty refers to p1 by aggregation, p1 owns its health record rec1 by composition, the two RiskAssessment objects a1 and a2 point to p1 through its id, and the PredictionService object currently holds the rule-based model as its strategy and the HighRiskNotifier as its observer.
> **Figure caption:** Figure 4: Object diagram of the system after one patient was assessed with both models.

---

## 5. Component Diagram

**Diagram name:** `COMP - Heart Risk System`
**Code evidence:** Java packages under `com.heartrisk`, `frontend/app.py`, `data/*.csv`.

### 5.1 Components (stereotype `«component»`)

| Exact name | Contents |
|---|---|
| `Streamlit Frontend` | `frontend/app.py` (pages Predict, History, Model Metrics, About) |
| `REST API` | package `api`: `ApiServer`, `ApiHandlers`, `RequestParser`, `ResponseMapper` |
| `Services` | package `service`: `PredictionService`, `PatientService`, `ModelCatalog`, `RecommendationService`, `ReportService`, `NotFoundException` |
| `Prediction Engine` | package `prediction`: `PredictionModel`, `Trainable`, `LogisticRegressionModel`, `RuleBasedModel`, `ModelFactory`, `ModelEvaluator`, `EvaluationResult` |
| `Domain Model` | package `model`: `User`, `Patient`, `Doctor`, `HealthRecord`, `RiskLevel`, `RiskAssessment` |
| `Data Access` | package `data`: `DatasetLoader`, `LabelledDataset`, `PatientRepository`, `CsvPatientRepository` |
| `Observer` | package `observer`: `AssessmentListener`, `HighRiskNotifier` |
| `Configuration` | package `config`: `ConfigManager` |
| `Bootstrap` | class `Main` (creates and wires all objects, starts the server) |

### 5.2 Other elements

| Type | Exact name |
|---|---|
| Artifact `«artifact»` | `heart.csv` |
| Artifact `«artifact»` | `patients.csv` |
| Artifact `«artifact»` | `assessments.csv` |
| Component `«library»` | `Javalin 6` |
| Component `«library»` | `Jackson` |
| Interface | `REST API (HTTP/JSON, port 7070)` |
| Interface | `PredictionModel` |
| Interface | `PatientRepository` |
| Interface | `AssessmentListener` |

### 5.3 Provided / required interfaces and dependencies

| From | To | UML type | Label |
|---|---|---|---|
| `REST API` | `REST API (HTTP/JSON, port 7070)` | Provided interface (ball) | |
| `Streamlit Frontend` | `REST API (HTTP/JSON, port 7070)` | Required interface (socket) / dependency | `requests` over HTTP |
| `Prediction Engine` | `PredictionModel` | Provided interface (ball) | |
| `Services` | `PredictionModel` | Required interface (socket) | Strategy |
| `Data Access` | `PatientRepository` | Provided interface (ball) | |
| `Services` | `PatientRepository` | Required interface (socket) | Repository |
| `Observer` | `AssessmentListener` | Provided interface (ball) | |
| `Services` | `AssessmentListener` | Required interface (socket) | Observer |
| `REST API` | `Services` | Dependency `«use»` | |
| `Services` | `Configuration` | Dependency `«use»` | split seed |
| `Prediction Engine` | `Configuration` | Dependency `«use»` | hyper-parameters |
| `Services` | `Domain Model` | Dependency `«use»` | |
| `Prediction Engine` | `Domain Model` | Dependency `«use»` | |
| `Data Access` | `Domain Model` | Dependency `«use»` | |
| `Observer` | `Domain Model` | Dependency `«use»` | |
| `Data Access` | `heart.csv` | Dependency `«read»` | `DatasetLoader` |
| `Data Access` | `patients.csv` | Dependency `«read/write»` | `CsvPatientRepository` |
| `Data Access` | `assessments.csv` | Dependency `«read/write»` | `CsvPatientRepository` |
| `REST API` | `Javalin 6` | Dependency `«use»` | |
| `REST API` | `Jackson` | Dependency `«use»` | |
| `Bootstrap` | `REST API`, `Services`, `Data Access`, `Observer`, `Configuration` | Dependency `«use»` (5 arrows) | wiring |

### 5.4 Build steps

1. Create the diagram; add the 9 components and the 3 artifacts; add the 2 libraries as components with `«library»`.
2. Add the four interfaces; use the *Provided*/*Required interface* tools (ball/socket) on the owning component.
3. Draw the dependencies top-down: Frontend, REST API, Services, then Prediction Engine / Data Access / Observer,
   then Domain Model and Configuration at the bottom, files at the far right.

> **Report text:** The component diagram groups the code into nine components that follow the Java packages. The Streamlit Frontend depends only on the HTTP/JSON interface of the REST API, the Services component uses the PredictionModel, PatientRepository and AssessmentListener interfaces provided by the Prediction Engine, Data Access and Observer components, and only Data Access touches the CSV files.
> **Figure caption:** Figure 5: Component diagram of the Heart Disease Risk Prediction System.

---

## 6. Sequence Diagram

**Diagram name:** `SD - Predict Risk`
**Scenario:** the user clicks *Predict* for a new patient (registration, then prediction with the logistic model, `HIGH` result).
**Code evidence:** `ApiHandlers.createPatient/predict`, `PatientService.register`, `PredictionService.assessWith/assess`.

### 6.1 Lifelines (left to right, exact names)

| # | Lifeline | Type / class |
|---|---|---|
| L1 | `User` | Actor |
| L2 | `app : StreamlitApp` | Streamlit page (`frontend/app.py`) |
| L3 | `server : ApiServer` | `ApiServer` |
| L4 | `handlers : ApiHandlers` | `ApiHandlers` |
| L5 | `patients : PatientService` | `PatientService` |
| L6 | `repo : CsvPatientRepository` | `CsvPatientRepository` |
| L7 | `catalog : ModelCatalog` | `ModelCatalog` |
| L8 | `predictions : PredictionService` | `PredictionService` |
| L9 | `model : LogisticRegressionModel` | implements `PredictionModel` |
| L10 | `notifier : HighRiskNotifier` | implements `AssessmentListener` |
| L11 | `advice : RecommendationService` | `RecommendationService` |

### 6.2 Messages in order (create exactly these, top to bottom)

| No. | From | To | Message text | Return (dashed arrow, unnumbered) |
|---|---|---|---|---|
| 1 | `User` | `app` | `clickPredict(name, email, features, model)` | |
| 2 | `app` | `server` | `POST /api/patients` | `201 {id}` |
| 3 | `server` | `handlers` | `createPatient(ctx)` | |
| 4 | `handlers` | `patients` | `register(name, email, builder)` | `Patient` |
| 5 | `patients` | `repo` | `nextPatientId()` | `"P-0001"` |
| 6 | `patients` | `repo` | `save(patient)` | |
| 7 | `app` | `server` | `POST /api/predict?model=logistic` | `200 {probability, level, model, recommendations}` |
| 8 | `server` | `handlers` | `predict(ctx)` | |
| 9 | `handlers` | `catalog` | `get("logistic")` | `PredictionModel` |
| 10 | `handlers` | `patients` | `getPatient(patientId)` | `Patient` |
| 11 | `patients` | `repo` | `findById(id)` | `Optional<Patient>` |
| 12 | `handlers` | `predictions` | `assessWith(model, patient)` | `RiskAssessment` |
| 13 | `predictions` | `predictions` | `setModel(model)` (self message) | |
| 14 | `predictions` | `predictions` | `assess(patient)` (self message) | |
| 15 | `predictions` | `model` | `predictProbability(record)` | `probability` |
| 16 | `predictions` | `repo` | `nextAssessmentId()` | `"A-0001"` |
| 17 | `predictions` | `repo` | `saveAssessment(assessment)` | |
| 18 | `predictions` | `notifier` | `onAssessmentCreated(assessment)` | |
| 19 | `handlers` | `advice` | `recommend(level, record)` | `List<String>` |
| 20 | `app` | `User` | `displayResult()` | |

Notes to add on the diagram:
* Note on message 4: `creates Patient; Patient builds and owns its HealthRecord (composition)`.
* Note on message 18: `inside onAssessmentCreated: if level == HIGH -> log alert, alertCount++`.
* Note on message 20: `the report text is fetched separately with GET /api/report/{assessmentId}` (not drawn).

### 6.3 Build steps

1. Create the diagram and add the 11 lifelines in the order of 6.1 (the actor first).
2. Add messages 1-20 top to bottom, naming them with the number prefix (`1: clickPredict(...)`). Use *synchronous* messages
   for calls and *return* messages (dashed) for the Return column; messages 13 and 14 are self messages on `predictions`.
3. Add activation bars (execution specifications) on `handlers`, `patients`, `predictions`.
4. Add the three notes.

> **Report text:** The sequence diagram traces the Predict Risk scenario. The Streamlit page first registers the patient through ApiServer, ApiHandlers and PatientService, which stores the new Patient via the CsvPatientRepository; it then requests a prediction, ApiHandlers obtains the model from ModelCatalog and PredictionService swaps in that strategy, calls predictProbability, stores the RiskAssessment and notifies the HighRiskNotifier before RecommendationService supplies the advice that is shown to the user.
> **Figure caption:** Figure 6: Sequence diagram of the Predict Risk scenario.

---

## 7. Communication Diagram

**Diagram name:** `COM - Predict Risk`
**Same scenario and the same message numbers 1-20 as the sequence diagram.** Return messages are not drawn.

### 7.1 Objects (create as lifelines/instances, exact names)

`User`, `app : StreamlitApp`, `server : ApiServer`, `handlers : ApiHandlers`, `patients : PatientService`,
`repo : CsvPatientRepository`, `catalog : ModelCatalog`, `predictions : PredictionService`,
`model : LogisticRegressionModel`, `notifier : HighRiskNotifier`, `advice : RecommendationService`.

### 7.2 Links (solid lines between objects) and the numbered messages on each

| Link between | Messages on the link (number: text, arrow direction) |
|---|---|
| `User` - `app` | `1: clickPredict(name, email, features, model)` (User -> app); `20: displayResult()` (app -> User) |
| `app` - `server` | `2: POST /api/patients`; `7: POST /api/predict?model=logistic` (both app -> server) |
| `server` - `handlers` | `3: createPatient(ctx)`; `8: predict(ctx)` (both server -> handlers) |
| `handlers` - `patients` | `4: register(name, email, builder)`; `10: getPatient(patientId)` (handlers -> patients) |
| `patients` - `repo` | `5: nextPatientId()`; `6: save(patient)`; `11: findById(id)` (patients -> repo) |
| `handlers` - `catalog` | `9: get("logistic")` (handlers -> catalog) |
| `handlers` - `predictions` | `12: assessWith(model, patient)` (handlers -> predictions) |
| `predictions` (self link) | `13: setModel(model)`; `14: assess(patient)` |
| `predictions` - `model` | `15: predictProbability(record)` (predictions -> model) |
| `predictions` - `repo` | `16: nextAssessmentId()`; `17: saveAssessment(assessment)` (predictions -> repo) |
| `predictions` - `notifier` | `18: onAssessmentCreated(assessment)` (predictions -> notifier) |
| `handlers` - `advice` | `19: recommend(level, record)` (handlers -> advice) |

### 7.3 Build steps

1. Create the diagram and add the 11 objects spread so that `handlers` is in the centre.
2. Draw the 12 links of 7.2 (11 between two objects, 1 self link).
3. Add the numbered messages to each link with arrows pointing in the stated direction. Number sequentially 1-20; do not use
   decimal numbering so that the order matches the sequence diagram.

> **Report text:** The communication diagram shows the same Predict Risk scenario as the sequence diagram but emphasises the links between objects instead of time. ApiHandlers is the hub that talks to PatientService, ModelCatalog, PredictionService and RecommendationService, PredictionService collaborates with the model, the repository and the observer, and the message numbers 1 to 20 give the same order as in the sequence diagram.
> **Figure caption:** Figure 7: Communication diagram of the Predict Risk scenario.

---

## 8. Deployment Diagram

**Diagram name:** `DEP - Heart Risk System`
**Setup shown:** everything runs on one computer during the live demonstration (three processes).

### 8.1 Nodes and artifacts

| Type | Exact name | Contains / deploys |
|---|---|---|
| Node `«device»` | `User Computer` | the four nodes below |
| Execution environment `«executionEnvironment»` | `Web Browser` | (nested in `User Computer`) shows the Streamlit page |
| Execution environment `«executionEnvironment»` | `Python 3.10+ / Streamlit Server` | (nested) artifacts `app.py`, `requirements.txt` |
| Execution environment `«executionEnvironment»` | `JVM (Java 17)` | (nested) artifact `heart-risk.jar` (contains Javalin 6, Jackson and the `com.heartrisk` classes) |
| Node `«fileSystem»` | `Data Folder` | (nested) artifacts `heart.csv`, `patients.csv`, `assessments.csv` |
| Artifact | `app.py`, `requirements.txt` | deployed on `Python 3.10+ / Streamlit Server` |
| Artifact | `heart-risk.jar` | deployed on `JVM (Java 17)` |
| Artifact | `heart.csv`, `patients.csv`, `assessments.csv` | deployed on `Data Folder` |

### 8.2 Communication paths

| Between | Stereotype / label | Direction |
|---|---|---|
| `Web Browser` - `Python 3.10+ / Streamlit Server` | `«HTTP»` `port 8501` | Browser calls Streamlit |
| `Python 3.10+ / Streamlit Server` - `JVM (Java 17)` | `«HTTP/JSON»` `port 7070` (Python `requests` -> Javalin) | Streamlit calls the Java REST API |
| `JVM (Java 17)` - `Data Folder` | `«file I/O»` | reads `heart.csv`, appends `patients.csv` and `assessments.csv` |

### 8.3 Build steps

1. Create `User Computer` and nest the four nodes inside it. 2. Add the artifacts and use the *Deployment*/*Manifestation*
tool (or drop the artifact inside the node) to deploy them. 3. Draw the three communication paths (association between nodes)
and type the stereotype and port as the label.

> **Report text:** The deployment diagram shows a single User Computer that hosts three runtime environments during the demonstration: a web browser that renders the Streamlit page on port 8501, the Streamlit server that calls the Java REST API with HTTP/JSON on port 7070, and the JVM that runs heart-risk.jar and reads and writes the CSV files in the data folder.
> **Figure caption:** Figure 8: Deployment diagram of the Heart Disease Risk Prediction System.

---

## 9. Final checklist before you export

- [ ] Names in every diagram are spelled exactly like the code (`PredictionService`, `assessWith`, `HealthRecord.Builder`, ...).
- [ ] Composition diamond is on `Patient`; aggregation diamond is on `Doctor`.
- [ ] Sequence and communication diagrams both use message numbers 1-20 in the same order.
- [ ] The object diagram uses the values from your own run (probability of `a1`, timestamps).
- [ ] All eight diagrams exported as PNG and inserted into the report at the `[INSERT DIAGRAM: ...]` placeholders.
- [ ] You can explain each diagram in 30 seconds during the viva.
