"""HeartRisk - Streamlit UI for the existing Java REST API.

The frontend deliberately contains no prediction logic. All patient validation,
model inference, risk classification, persistence and report generation remain
in the Java backend.
"""
import os
from datetime import datetime

import pandas as pd
import requests
import streamlit as st

API_URL = os.environ.get("HEART_API_URL", "http://localhost:7071").rstrip("/")
DISCLAIMER = "Educational decision-support project only — not a medical diagnosis."
MODELS = {"Logistic Regression": "logistic", "Rule-Based Score": "rule"}

# Keep the backend's exact feature keys and accepted ranges.
FEATURES = [
    ("age", "Age", 1, 120, 55, 1, "Age in years."),
    ("sex", "Sex", 0, 1, 1, 1, "0 = female, 1 = male."),
    ("cp", "Chest pain type", 0, 3, 0, 1, "0 = typical angina; 1 = atypical angina; 2 = non-anginal pain; 3 = asymptomatic."),
    ("trestbps", "Resting blood pressure", 80, 220, 130, 1, "mm Hg."),
    ("chol", "Serum cholesterol", 100, 600, 240, 1, "mg/dL."),
    ("fbs", "Fasting blood sugar > 120 mg/dL", 0, 1, 0, 1, "0 = no, 1 = yes."),
    ("restecg", "Resting ECG result", 0, 2, 1, 1, "0 = normal; 1 = ST-T wave abnormality; 2 = left ventricular hypertrophy."),
    ("thalach", "Maximum heart rate achieved", 60, 220, 150, 1, "Beats per minute."),
    ("exang", "Exercise-induced angina", 0, 1, 0, 1, "0 = no, 1 = yes."),
    ("oldpeak", "ST depression (oldpeak)", 0.0, 7.0, 1.0, 0.1, "ST depression induced by exercise relative to rest."),
    ("slope", "Slope of peak exercise ST segment", 0, 2, 1, 1, "0 = upsloping; 1 = flat; 2 = downsloping."),
    ("ca", "Major vessels (ca)", 0, 4, 0, 1, "Number of major vessels coloured by fluoroscopy (0–4)."),
    ("thal", "Thalassemia dataset code", 0, 3, 2, 1, "Dataset code from 0–3."),
]
CHOICES = {
    "sex": {0: "Female", 1: "Male"},
    "cp": {0: "Typical angina", 1: "Atypical angina", 2: "Non-anginal pain", 3: "Asymptomatic"},
    "fbs": {0: "No", 1: "Yes"},
    "restecg": {0: "Normal", 1: "ST-T wave abnormality", 2: "Left ventricular hypertrophy"},
    "exang": {0: "No", 1: "Yes"},
    "slope": {0: "Upsloping", 1: "Flat", 2: "Downsloping"},
    "thal": {0: "Code 0", 1: "Fixed defect", 2: "Normal", 3: "Reversible defect"},
}

st.set_page_config(page_title="HeartRisk | Clinical Assessment", page_icon="❤️", layout="wide", initial_sidebar_state="expanded")

CUSTOM_CSS = """
<style>
@import url('https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500;600;700&family=Manrope:wght@400;500;600;700;800&display=swap');
:root { --navy:#10263f; --navy2:#173653; --teal:#087f78; --teal-soft:#e2f5f2; --canvas:#f4f7fb; --ink:#142b43; --muted:#66788b; }
html, body, [class*="css"] { font-family:'DM Sans',sans-serif; }
.stApp { background:var(--canvas); color:var(--ink); }
[data-testid="stHeader"] { background:transparent; }
.block-container { padding-top:1.6rem; padding-bottom:2.5rem; max-width:1450px; }
section[data-testid="stSidebar"] { background:var(--navy); border-right:1px solid #25415b; }
section[data-testid="stSidebar"] * { color:#edf5fb !important; }
section[data-testid="stSidebar"] [data-testid="stRadio"] label { padding: .55rem .7rem; border-radius:9px; }
section[data-testid="stSidebar"] [data-testid="stRadio"] label:hover { background:#1c3a56; }
section[data-testid="stSidebar"] [data-testid="stMarkdownContainer"] p { color:#c4d4e2 !important; }
.brand { font-family:Manrope,sans-serif; font-size:1.42rem; font-weight:800; letter-spacing:-.04em; color:#fff; }
.brand-sub { color:#9edbd4; font-size:.69rem; letter-spacing:.13em; text-transform:uppercase; font-weight:700; }
.eyebrow { color:var(--teal); font-size:.72rem; text-transform:uppercase; letter-spacing:.14em; font-weight:800; margin-bottom:.35rem; }
.page-title { font-family:Manrope,sans-serif; font-size:2rem; line-height:1.15; font-weight:800; letter-spacing:-.045em; color:var(--ink); margin:0; }
.page-subtitle { color:var(--muted); font-size:.96rem; margin-top:.45rem; }
.panel { background:#fff; border:1px solid #e5ebf2; border-radius:16px; padding:1.15rem 1.25rem; box-shadow:0 5px 18px rgba(18,42,67,.035); }
.panel-title { font-family:Manrope,sans-serif; font-weight:800; font-size:1.02rem; color:var(--ink); margin-bottom:.2rem; }
.panel-note { color:var(--muted); font-size:.83rem; }
.kicker { color:var(--muted); font-size:.72rem; text-transform:uppercase; letter-spacing:.1em; font-weight:800; }
.hero { background:linear-gradient(115deg,#10263f 0%,#174563 68%,#087f78 130%); color:white; padding:1.65rem 1.8rem; border-radius:18px; margin-bottom:1.1rem; box-shadow:0 12px 28px rgba(16,38,63,.13); }
.hero h2 { font-family:Manrope,sans-serif; font-size:1.75rem; line-height:1.2; font-weight:800; color:white; margin:.25rem 0 .5rem; }
.hero p { color:#d6e7f0; margin:0; max-width:720px; }
div[data-testid="stMetric"] { background:#fff; border:1px solid #e5ebf2; padding:1rem 1.05rem; border-radius:14px; box-shadow:0 4px 14px rgba(18,42,67,.025); }
div[data-testid="stMetricLabel"] { color:#64778a; }
div[data-testid="stMetricValue"] { color:var(--ink); font-family:Manrope,sans-serif; font-weight:800; }
.stButton > button, .stDownloadButton > button, [data-testid="stFormSubmitButton"] button { border-radius:9px; font-weight:700; min-height:2.65rem; }
.stButton > button[kind="primary"], [data-testid="stFormSubmitButton"] button[kind="primary"] { background:var(--teal); border-color:var(--teal); }
.stButton > button[kind="primary"]:hover, [data-testid="stFormSubmitButton"] button[kind="primary"]:hover { background:#066b65; border-color:#066b65; }
div[data-baseweb="input"] > div, div[data-baseweb="select"] > div { border-radius:9px; }
hr { border-color:#e4eaf1; }
.small-muted { color:#718195; font-size:.78rem; }
.risk-low { color:#087443; background:#e6f6ed; padding:.35rem .7rem; border-radius:99px; font-weight:800; display:inline-block; }
.risk-medium { color:#986000; background:#fff3d9; padding:.35rem .7rem; border-radius:99px; font-weight:800; display:inline-block; }
.risk-high { color:#b42318; background:#ffebe9; padding:.35rem .7rem; border-radius:99px; font-weight:800; display:inline-block; }
div[data-testid="stAlert"] { border-radius:12px; }
footer { visibility:hidden; }
</style>
"""
st.markdown(CUSTOM_CSS, unsafe_allow_html=True)


class ApiError(Exception):
    """A readable error returned by the Java API or network layer."""


def call(method, path, **kwargs):
    try:
        response = requests.request(method, API_URL + path, timeout=20, **kwargs)
    except requests.exceptions.ConnectionError as exc:
        raise ApiError(f"Cannot reach the Java backend at {API_URL}. Start the backend first, then refresh this page.") from exc
    except requests.exceptions.Timeout as exc:
        raise ApiError("The Java backend did not respond within 20 seconds.") from exc
    if response.status_code >= 400:
        try:
            message = response.json().get("error", response.text)
        except ValueError:
            message = response.text
        raise ApiError(f"{message} (HTTP {response.status_code})")
    return response


def api_status():
    try:
        data = call("GET", "/api/health").json()
        return True, data
    except (ApiError, ValueError):
        return False, {}


def page_header(eyebrow, title, subtitle):
    st.markdown(f'<div class="eyebrow">{eyebrow}</div><h1 class="page-title">{title}</h1><div class="page-subtitle">{subtitle}</div>', unsafe_allow_html=True)
    st.write("")


def risk_badge(level):
    cls = {"LOW": "risk-low", "MEDIUM": "risk-medium", "HIGH": "risk-high"}.get(level, "")
    st.markdown(f'<span class="{cls}">{level} RISK</span>', unsafe_allow_html=True)


def model_label_from_api(model):
    if "Rule" in str(model):
        return "Rule-Based Score"
    return "Logistic Regression"


def render_sidebar(is_online):
    with st.sidebar:
        st.markdown('<div style="padding:.55rem .25rem 1.1rem"><div style="font-size:1.6rem">♥</div><div class="brand">HeartRisk</div><div class="brand-sub">Clinical assessment suite</div></div>', unsafe_allow_html=True)
        st.markdown("<div style='height:1px;background:#29445e;margin:.3rem 0 1rem'></div>", unsafe_allow_html=True)
        pages = ["Dashboard", "New Assessment", "Prediction Results", "Patient History", "Model Performance", "About System"]
        page = st.radio("NAVIGATION", pages, label_visibility="collapsed", key="navigation")
        st.markdown("<div style='height:1px;background:#29445e;margin:1rem 0'></div>", unsafe_allow_html=True)
        if is_online:
            st.markdown("🟢 **Java API connected**")
            st.caption(f"{API_URL}")
        else:
            st.markdown("🔴 **Java API offline**")
            st.caption("Start the backend to use assessments.")
        st.markdown("<div style='height:1px;background:#29445e;margin:1rem 0'></div>", unsafe_allow_html=True)
        st.caption(DISCLAIMER)
    return page


def dashboard_page(is_online, health_data):
    page_header("HEART HEALTH WORKSPACE", "Clinical Assessment Dashboard", "A focused workspace for patient registration, heart-disease risk estimation and model evaluation.")
    model_names = ", ".join(health_data.get("models", ["logistic", "rule"])) if health_data else "Logistic Regression · Rule-Based Score"
    st.markdown(f'''<div class="hero"><div class="brand-sub">HEARTRISK ·</div><h2>Understand risk. Support better conversations.</h2><p>Assess the 13 medical attributes used by the UCI Cleveland dataset, compare two prediction approaches and review the assessment output.</p></div>''', unsafe_allow_html=True)
    a, b, c, d = st.columns(4)
    a.metric("Medical inputs", "13", help="The 13 predictor features used by the supplied dataset.")
    b.metric("Prediction models", "02", help="Logistic Regression and Rule-Based Score.")
    c.metric("Evaluation split", "80 / 20", help="Training and testing split configured in the backend.")
    d.metric("OOAD patterns", "06", help="Strategy, Factory, Singleton, Builder, Observer and Repository.")
    st.write("")
    left, right = st.columns([1.25, 1], gap="large")
    with left:
        st.markdown('<div class="panel"><div class="eyebrow">GET STARTED</div><div class="panel-title">Start a patient assessment</div><div class="panel-note">Enter patient details and the 13 required health-record attributes. The Java backend validates the inputs and runs the selected model.</div></div>', unsafe_allow_html=True)
        if st.button("＋  Create new assessment", type="primary", use_container_width=True):
            st.session_state["next_page"] = "New Assessment"
            st.rerun()
    with right:
        st.markdown('<div class="panel"><div class="eyebrow">SYSTEM STATUS</div><div class="panel-title">Backend connection</div>', unsafe_allow_html=True)
        if is_online:
            st.success("Java REST API is responding.")
            st.caption("Patient registration, prediction, history, metrics and report endpoints are available.")
        else:
            st.error("Java REST API is not reachable.")
            st.caption("Start the backend using run_windows.bat or the manual instructions in README.md.")
        st.markdown('</div>', unsafe_allow_html=True)
    st.write("")
    st.markdown('<div class="panel-title">Available prediction approaches</div>', unsafe_allow_html=True)
    m1, m2 = st.columns(2)
    with m1:
        st.markdown('<div class="panel"><div class="eyebrow">DATA-DRIVEN</div><div class="panel-title">Logistic Regression</div><div class="panel-note">A supervised binary-classification model trained by the Java backend using batch gradient descent. It returns a model-estimated probability.</div></div>', unsafe_allow_html=True)
    with m2:
        st.markdown('<div class="panel"><div class="eyebrow">RULE-BASED</div><div class="panel-title">Rule-Based Score</div><div class="panel-note">A transparent points-based score from seven predefined conditions. Its normalized score is not a clinically calibrated probability.</div></div>', unsafe_allow_html=True)
    recent = st.session_state.get("result")
    if recent:
        st.write("")
        st.markdown('<div class="panel-title">Last assessment in this session</div>', unsafe_allow_html=True)
        x, y, z = st.columns(3)
        x.metric("Patient ID", recent.get("patientId", "—"))
        y.metric("Assessment ID", recent.get("assessmentId", "—"))
        z.metric("Risk classification", recent.get("level", "—"))


def render_feature_form():
    values = {}
    groups = [
        ("Patient details", ["name", "email"]),
        ("Personal information", ["age", "sex"]),
        ("Cardiovascular measurements", ["cp", "trestbps", "chol", "fbs"]),
        ("ECG and exercise information", ["restecg", "thalach", "exang", "oldpeak", "slope"]),
        ("Additional dataset attributes", ["ca", "thal"]),
    ]
    feature_map = {item[0]: item for item in FEATURES}
    try:
        doctor_data = call("GET", "/api/doctors").json().get("doctors", [])
    except ApiError as error:
        st.error(str(error))
        return
    if not doctor_data:
        st.error("No doctors are currently available. Please contact the system administrator.")
        return
    doctor_options = {f"{d['name']} — {d['specialty']} ({d['id']})": d["id"] for d in doctor_data}
    with st.form("assessment_form", clear_on_submit=False):
        st.markdown('<div class="panel-title">Patient information</div><div class="panel-note">These details identify the patient record saved by the backend.</div>', unsafe_allow_html=True)
        p1, p2 = st.columns(2)
        with p1:
            name = st.text_input("Patient full name", value="Demo Patient", placeholder="Enter patient name")
        with p2:
            email = st.text_input("Email address", value="demo@example.com", placeholder="name@example.com")
        doctor_label = st.selectbox("Assign to doctor", list(doctor_options.keys()), help="Choose the doctor who will be responsible for reviewing this patient's assessment.")
        doctor_id = doctor_options[doctor_label]
        st.divider()
        for group_name, keys in groups[1:]:
            st.markdown(f'<div class="panel-title">{group_name}</div>', unsafe_allow_html=True)
            cols = st.columns(2)
            for idx, key in enumerate(keys):
                item = feature_map[key]
                _, label, low, high, default, step, help_text = item
                with cols[idx % 2]:
                    if key in CHOICES:
                        options = list(CHOICES[key].keys())
                        values[key] = st.selectbox(label, options, index=options.index(default), format_func=lambda v, k=key: f"{v} — {CHOICES[k][v]}", help=help_text, key=f"input_{key}")
                    elif key == "oldpeak":
                        values[key] = st.number_input(label, min_value=float(low), max_value=float(high), value=float(default), step=float(step), help=help_text, key=f"input_{key}")
                    else:
                        values[key] = st.number_input(label, min_value=int(low), max_value=int(high), value=int(default), step=int(step), help=help_text, key=f"input_{key}")
            st.write("")
        st.divider()
        model_label = st.selectbox("Prediction model", list(MODELS), help="Choose which backend model will assess this patient.")
        submitted = st.form_submit_button("Generate risk assessment", type="primary", use_container_width=True)
    if submitted:
        if not name.strip() or not email.strip():
            st.error("Please enter both the patient's name and email address.")
            return
        try:
            signature = (name.strip(), email.strip(), doctor_id, tuple(values.items()))
            cached = st.session_state.get("patient")
            if cached and cached.get("signature") == signature:
                patient_id = cached["id"]
            else:
                created = call("POST", "/api/patients", json={"name": name.strip(), "email": email.strip(), "doctorId": doctor_id, "features": values}).json()
                patient_id = created["id"]
                st.session_state["patient"] = {"signature": signature, "id": patient_id}
            result = call("POST", "/api/predict", params={"model": MODELS[model_label]}, json={"patientId": patient_id}).json()
            result["report"] = call("GET", f"/api/report/{result['assessmentId']}").text
            result["patientName"] = name.strip()
            result["doctorName"] = doctor_label
            result["doctorId"] = doctor_id
            result["modelLabel"] = model_label
            result["features"] = values.copy()
            st.session_state["result"] = result
            st.session_state["next_page"] = "Prediction Results"
            st.success("Assessment completed successfully.")
            st.rerun()
        except ApiError as error:
            st.error(str(error))


def prediction_page():
    page_header("PATIENT INTAKE", "New heart-risk assessment", "Complete the patient details and all 13 medical attributes used by the prediction models.")
    st.info("Enter values from the patient's available record. The system is an educational project and does not replace clinical assessment.")
    render_feature_form()


def results_page():
    page_header("ASSESSMENT OUTPUT", "Prediction results & risk analysis", "The values below are returned by your Java prediction service; no sample results are fabricated.")
    result = st.session_state.get("result")
    if not result:
        st.info("No assessment is available in this session yet. Create a new assessment to see its result here.")
        if st.button("Start a new assessment", type="primary"):
            st.session_state["next_page"] = "New Assessment"
            st.rerun()
        return
    level = result["level"]
    score = float(result["probability"])
    model = result.get("modelLabel", model_label_from_api(result.get("model", "")))
    is_rule = "Rule" in model
    c1, c2 = st.columns([1.1, 1], gap="large")
    with c1:
        st.markdown('<div class="panel"><div class="eyebrow">DIAGNOSTIC RISK CLASSIFICATION</div><div class="panel-title">Patient assessment summary</div>', unsafe_allow_html=True)
        st.markdown(f"### {result.get('patientName', 'Patient')}")
        st.caption(f"Patient ID: {result.get('patientId', '—')}  ·  Assessment ID: {result.get('assessmentId', '—')}")
        if result.get("doctorName"):
            st.caption(f"Assigned doctor: {result['doctorName']}")
        risk_badge(level)
        metric_label = "Normalized rule score" if is_rule else "Model-estimated probability"
        st.metric(metric_label, f"{score * 100:.1f}%")
        st.progress(max(0.0, min(1.0, score)), text=f"{metric_label}: {score * 100:.1f}%")
        st.markdown(f"**Prediction model:** {model}")
        if is_rule:
            st.warning("This is a points-based normalized score, not a calibrated medical probability.")
        else:
            st.caption("This is the model's estimated probability based on the supplied features and its training data.")
        st.markdown('</div>', unsafe_allow_html=True)
    with c2:
        st.markdown('<div class="panel"><div class="eyebrow">RECOMMENDATIONS</div><div class="panel-title">Assessment guidance</div><div class="panel-note">Recommendations returned by the backend for this assessment.</div>', unsafe_allow_html=True)
        for advice in result.get("recommendations", []):
            st.markdown(f"- {advice}")
        st.markdown('</div>', unsafe_allow_html=True)
    st.write("")
    a, b = st.columns([1, 1])
    with a:
        st.download_button("⬇ Download assessment report (.txt)", result.get("report", ""), file_name=f"heart_risk_{result.get('assessmentId', 'report')}.txt", mime="text/plain", use_container_width=True)
    with b:
        if st.button("＋ Perform another assessment", use_container_width=True):
            st.session_state["next_page"] = "New Assessment"
            st.rerun()
    st.caption(DISCLAIMER)


def history_page():
    page_header("PATIENT RECORDS", "Assessment history", "All registered patients are shown automatically. Search by patient name, email or ID to narrow the list, then expand a patient to view their assessments.")
    try:
        data = call("GET", "/api/patients").json()
        patients = data.get("patients", [])
        if not patients:
            st.info("No patients have been registered yet. Complete a new assessment to add the first patient.")
            return

        query = st.text_input("Search patients", placeholder="Search by name, email or patient ID...").strip().lower()
        filtered = [p for p in patients if not query or query in str(p.get("name", "")).lower()
                    or query in str(p.get("email", "")).lower() or query in str(p.get("id", "")).lower()]
        total_assessments = sum(len(p.get("assessments", [])) for p in patients)
        c1, c2, c3 = st.columns(3)
        c1.metric("Registered patients", len(patients))
        c2.metric("Total assessments", total_assessments)
        c3.metric("Search results", len(filtered))
        st.write("")
        if not filtered:
            st.warning("No patients match that search. Try another name, email or ID.")
            return

        summary_rows = []
        for patient in filtered:
            records = patient.get("assessments", [])
            latest = records[-1] if records else {}
            summary_rows.append({
                "Patient ID": patient.get("id", "—"),
                "Patient name": patient.get("name", "—"),
                "Email": patient.get("email", "—"),
                "Assigned doctor": patient.get("doctorName", patient.get("doctorId", "—")),
                "Assessments": len(records),
                "Latest risk": latest.get("level", "Not assessed"),
            })
        st.dataframe(pd.DataFrame(summary_rows), use_container_width=True, hide_index=True)
        st.caption(f"Showing {len(filtered)} of {len(patients)} registered patient(s). Select a patient below to view their full assessment history.")

        for patient in filtered:
            records = patient.get("assessments", [])
            title = f"{patient.get('name', 'Patient')} · {patient.get('id', '')} · {len(records)} assessment(s)"
            with st.expander(title):
                a, b, c = st.columns(3)
                a.write(f"**Email:** {patient.get('email', '—')}")
                b.write(f"**Assigned doctor:** {patient.get('doctorName', patient.get('doctorId', '—'))}")
                c.write(f"**Patient ID:** {patient.get('id', '—')}")
                if records:
                    frame = pd.DataFrame(records)
                    if "probability" in frame:
                        frame["probability"] = (frame["probability"].astype(float) * 100).round(1).astype(str) + "%"
                    frame = frame.rename(columns={"assessmentId": "Assessment ID", "patientId": "Patient ID", "probability": "Score / probability", "level": "Risk level", "model": "Model", "timestamp": "Date (UTC)"})
                    st.dataframe(frame, use_container_width=True, hide_index=True)
                else:
                    st.info("This patient is registered but has no saved assessments yet.")
    except ApiError as error:
        st.error(str(error))


def fetch_metrics(model_key):
    return call("GET", "/api/model/metrics", params={"model": model_key}).json()


def model_performance_page():
    page_header("MODEL EVALUATION", "Model performance", "Review test-split metrics returned by the Java backend for each prediction approach.")
    selected = st.selectbox("Select model", list(MODELS))
    try:
        metrics = fetch_metrics(MODELS[selected])
        cols = st.columns(4)
        for col, label, key in zip(cols, ["Accuracy", "Precision", "Recall", "F1 score"], ["accuracy", "precision", "recall", "f1"]):
            col.metric(label, f"{float(metrics[key]):.3f}")
        st.write("")
        left, right = st.columns([1, 1], gap="large")
        matrix = metrics["confusionMatrix"]
        with left:
            st.markdown('<div class="panel"><div class="panel-title">Confusion matrix</div><div class="panel-note">Rows are actual classes; columns are predicted classes.</div>', unsafe_allow_html=True)
            matrix_df = pd.DataFrame(
                [[matrix["trueNegatives"], matrix["falsePositives"]], [matrix["falseNegatives"], matrix["truePositives"]]],
                index=["Actual: no disease", "Actual: disease"],
                columns=["Predicted: no disease", "Predicted: disease"],
            )
            st.dataframe(matrix_df, use_container_width=True)
            st.markdown('</div>', unsafe_allow_html=True)
        with right:
            st.markdown('<div class="panel"><div class="panel-title">Evaluation sample</div>', unsafe_allow_html=True)
            st.metric("Training rows", metrics.get("trainSamples", "—"))
            st.metric("Testing rows", metrics.get("testSamples", "—"))
            st.markdown('</div>', unsafe_allow_html=True)
        st.write("")
        st.markdown('<div class="panel"><div class="panel-title">How to interpret these metrics</div><div class="panel-note">Accuracy is the overall fraction of correct predictions. Precision measures how often positive predictions are correct. Recall measures how many actual positive cases are detected. F1 is the harmonic mean of precision and recall. For screening-oriented discussion, pay particular attention to false negatives and recall.</div></div>', unsafe_allow_html=True)
        st.caption("Dataset limitation: the supplied CSV contains duplicate records, so train/test metrics may be optimistic if duplicates appear in both splits.")
    except ApiError as error:
        st.error(str(error))


def about_page():
    page_header("PROJECT INFORMATION", "About HeartRisk", "An Object-Oriented Analysis & Design project demonstrating an end-to-end prediction application.")
    st.markdown('<div class="hero"><div class="brand-sub">CT-263 · OOAD</div><h2>Heart Disease Risk Prediction System</h2><p>The application estimates heart-disease risk from 13 features of the UCI Cleveland dataset using two Java models and presents the result through a Streamlit frontend.</p></div>', unsafe_allow_html=True)
    c1, c2 = st.columns(2)
    with c1:
        st.markdown('<div class="panel"><div class="panel-title">Technology stack</div><br>**Backend:** Java 17, Javalin REST API, Jackson and Maven<br><br>**Frontend:** Python, Streamlit, Pandas and Requests<br><br>**Testing:** JUnit 5<br><br>**Storage:** CSV files</div>', unsafe_allow_html=True)
    with c2:
        st.markdown('<div class="panel"><div class="panel-title">Prediction models</div><br>**Logistic Regression:** supervised binary classification trained using batch gradient descent.<br><br>**Rule-Based Score:** transparent points-based score calculated from seven predefined conditions.</div>', unsafe_allow_html=True)
    st.write("")
    st.markdown('<div class="panel"><div class="panel-title">Object-oriented design patterns</div><br>**Strategy** — interchangeable prediction algorithms.<br>**Factory** — centralized model creation.<br>**Singleton** — shared application configuration.<br>**Builder** — validated construction of the 13-feature HealthRecord.<br>**Observer** — high-risk assessment notification.<br>**Repository** — separation of patient persistence from services.</div>', unsafe_allow_html=True)
    st.write("")
    st.warning(DISCLAIMER)


def main():
    # Apply requested navigation before the sidebar radio widget is instantiated.
    next_page = st.session_state.pop("next_page", None)
    if next_page:
        st.session_state["navigation"] = next_page
    is_online, health_data = api_status()
    page = render_sidebar(is_online)
    try:
        if page == "Dashboard":
            dashboard_page(is_online, health_data)
        elif page == "New Assessment":
            prediction_page()
        elif page == "Prediction Results":
            results_page()
        elif page == "Patient History":
            history_page()
        elif page == "Model Performance":
            model_performance_page()
        else:
            about_page()
    except ApiError as error:
        st.error(str(error))
    st.markdown("<div style='margin-top:2rem;padding-top:1rem;border-top:1px solid #e4eaf1;color:#78899a;font-size:.75rem'>HeartRisk · CT-263 OOAD · Educational tool only, not a medical diagnosis.</div>", unsafe_allow_html=True)


if __name__ == "__main__":
    main()
