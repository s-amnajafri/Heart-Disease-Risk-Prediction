# Heart Disease Risk Prediction System

CT-263 Object-Oriented Analysis & Design (CCA + CCP) project. A Java 17 REST backend (Javalin 6, Jackson, no ML libraries) with a redesigned Streamlit frontend. It estimates heart-disease risk from the 13 features of the UCI Cleveland data set using two models written from scratch (logistic regression and a rule-based score), and demonstrates classes, objects, inheritance, composition, aggregation and six design patterns.

> **Educational tool only, not a medical diagnosis.**

## Quick start on Windows

1. Install **JDK 17 or newer**, **Maven 3.8 or newer**, and **Python 3.10 or newer**. Make sure `java`, `mvn`, and the Python launcher `py` work in a new terminal.
2. Extract the complete project folder.
3. Double-click `run_windows.bat`.
4. The launcher creates `frontend/.venv` if needed, installs Python requirements, builds/starts the Java backend in one terminal, and starts Streamlit in another.
5. Open **http://localhost:8501** if the browser does not open automatically.

Keep both terminal windows open while using the app. Close them to stop the application. The backend runs on port **7070** and the Streamlit frontend on port **8501**.

## Manual start (Windows, macOS or Linux)

Terminal 1 - Java backend:

```bash
cd backend
mvn clean package
java -jar target/heart-risk.jar
```

Terminal 2 - Streamlit frontend:

```bash
cd frontend
python -m venv .venv
```

Activate the environment:

```powershell
# Windows PowerShell
.venv\Scripts\Activate.ps1
```

```bash
# macOS / Linux
source .venv/bin/activate
```

Then install and run:

```bash
pip install -r requirements.txt
streamlit run app.py
```

Open **http://localhost:8501**. To use a different backend address, set `HEART_API_URL` (default `http://localhost:7070`). An example is provided in `frontend/.env.example`; the app reads the environment variable, not the `.env` file automatically.

## Frontend screens

- **Dashboard:** project overview, actual API connection status, model descriptions and project facts. It intentionally does not invent global patient totals because the backend has no all-patients/statistics endpoint.
- **New Assessment:** patient name/email and all 13 health-record features. Coded dataset values are shown with their labels while the exact integer codes are sent to the backend.
- **Prediction Results:** displays the actual response from the selected Java model, risk level, recommendations and the existing plain-text report download. The Rule-Based output is labelled as a normalized score, not a calibrated probability.
- **Patient History:** retrieves assessments by patient ID.
- **Model Performance:** retrieves accuracy, precision, recall, F1 and confusion matrix from the backend.
- **About System:** project, technology and design-pattern information.

The Stitch screen is used as a visual reference (navy sidebar, pale clinical canvas, teal accents, cards and typography). Unsupported mock features such as SHAP explanations, ECG waveforms, fluoroscopy images, doctor assignment, PDF/CSV reports and clinical guideline directives were not added.

## Prerequisites

| Tool | Version | Check |
|---|---|---|
| JDK | 17 or newer | `java -version` |
| Maven | 3.8 or newer | `mvn -version` |
| Python | 3.10 or newer | `python --version` |

Place the data set at `data/heart.csv` (14 columns: `age,sex,cp,trestbps,chol,fbs,restecg,thalach,exang,oldpeak,slope,ca,thal,target`).

On startup, the backend loads the data set, trains both models once (80/20 split, seed 42), and prints the logistic-regression test accuracy. New patients and assessments are appended to `data/patients.csv` and `data/assessments.csv` (created automatically). The patient intake form lets the user choose one of four configured doctors; each patient's doctor ID is persisted with the patient record.

Run only the backend tests with `cd backend && mvn test`.

## REST API (JSON, CORS enabled, errors as `{"error": "...", "status": 400}`)

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/health` | Liveness check |
| POST | `/api/patients` | Register a patient: `{"name","email","features":{13 values}}` -> 201 with patient ID |
| POST | `/api/predict?model=logistic\|rule` | Assess an existing patient: `{"patientId":"P-0001"}` |
| GET | `/api/patients/{id}/history` | Past assessments for one patient |
| GET | `/api/model/metrics?model=logistic\|rule` | Test-split metrics |
| GET | `/api/report/{assessmentId}` | Plain-text report |

## Feature ranges

`age` 1-120, `sex` 0/1, `cp` 0-3, `trestbps` 80-220, `chol` 100-600, `fbs` 0/1, `restecg` 0-2, `thalach` 60-220, `exang` 0/1, `oldpeak` 0-7, `slope` 0-2, `ca` 0-4, `thal` 0-3. Invalid values return HTTP 400.

## Notes about the data set

- **Label direction:** in the supplied `heart.csv`, `target = 0` marks patients with heart disease. `DatasetLoader` maps this value to disease-present. If replacing the data set with differently encoded labels, update `DISEASE_TARGET_VALUE` in `DatasetLoader`.
- **Missing values:** rows containing `?`, blank or `NA` are skipped and counted.
- **Duplicates:** the supplied file has 1,025 rows but only 302 distinct records, so duplicates can occur in both training and testing. Test-split metrics may therefore be optimistic; mention this as a limitation.

## OOAD concepts

| Concept | Where |
|---|---|
| Inheritance / abstraction | abstract `User` -> `Patient`, `Doctor` |
| Composition | `Patient` owns one `HealthRecord` |
| Aggregation | `Doctor` holds `List<Patient>` |
| Encapsulation | private fields, getters, validation in `HealthRecord.Builder.build()` |
| Interface / polymorphism | `PredictionModel` implemented by both models |
| Enum | `RiskLevel` (< 0.33 LOW, < 0.66 MEDIUM, else HIGH) |

## Design patterns

| Pattern | Classes | Purpose |
|---|---|---|
| Strategy | `PredictionModel`, both models, `PredictionService` | Swap prediction algorithm |
| Factory | `ModelFactory` | Centralized model creation |
| Singleton | `ConfigManager.getInstance()` | One shared configuration |
| Builder | `HealthRecord.Builder` | Readable validated construction |
| Observer | `AssessmentListener`, `HighRiskNotifier`, `PredictionService` | Notify when HIGH risk is created |
| Repository | `PatientRepository`, `CsvPatientRepository` | Hide CSV persistence behind an interface |

## Documentation

- `docs/diagrams/modelio-build-sheets.md` - build sheets for the UML diagrams.
- `docs/viva-cheatsheet.md` - likely viva questions and short answers.
- `report/Heart_Disease_Risk_Report.docx` - CCP report.


### Patient assignment and history

- `GET /api/doctors` returns the doctors available in the patient-intake dropdown.
- `POST /api/patients` now requires `doctorId` alongside the patient's name, email and feature values.
- `GET /api/patients` returns every registered patient and their saved assessments.
- `GET /api/patients/{id}/history` continues to return one patient's assessments.

The History page displays the full patient list by default, supports searching by patient name, email or ID, and expands each patient to show all of their assessments. Existing `patients.csv` files are automatically migrated on startup by adding a `doctorId` column; records created before this change are assigned to `D-001`. This is a lightweight assignment workflow, not a login or authentication system.
