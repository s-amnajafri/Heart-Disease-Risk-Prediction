# Frontend redesign notes

The Streamlit frontend was redesigned using the supplied Stitch screen as a visual reference. The implementation intentionally preserves the Java API and existing project scope.

## Visual direction adopted

- Deep navy navigation sidebar.
- Pale blue-grey page background and white content panels.
- Teal primary actions and clinical-style accent colors.
- Manrope / DM Sans typography, compact labels and card-based grouping.
- Separate screens for Dashboard, New Assessment, Prediction Results, Patient History, Model Performance and About System.

## Functional mapping

| UI area | Existing API / data used |
|---|---|
| Backend status | `GET /api/health` |
| Patient assessment | `POST /api/patients` |
| Prediction result | `POST /api/predict?model=logistic` or `model=rule` |
| Recommendations | Returned by the prediction endpoint |
| Text report download | `GET /api/report/{assessmentId}` |
| Patient history | `GET /api/patients/{id}/history` |
| Model performance | `GET /api/model/metrics?model=logistic` or `model=rule` |

## Scope decisions

The Stitch concept screen included several elements that are not implemented by the supplied backend. These were deliberately not reproduced as working-looking features:

- SHAP feature attribution / contribution percentages.
- ECG waveform or 12-lead tracing.
- Coronary imaging, stenosis or perfusion analysis.
- QTc interval and arrhythmia detection.
- Clinical guideline directives or medication prescriptions.
- Doctor assignment / doctor profile workflows.
- PDF or CSV report generation.

The existing backend generates a plain-text report, so the UI keeps the `.txt` download. The Rule-Based model returns a points-based normalized score; the UI labels it as a score and does not present it as a calibrated medical probability. The Logistic Regression result is labelled as the model-estimated probability.

## Honest dashboard content

The backend does not expose global patient counts or aggregate assessment statistics. Therefore the dashboard shows fixed project facts (13 input features, two prediction models, 80/20 split and six design patterns), API connection status, and the last assessment from the current Streamlit session rather than inventing system-wide totals.
