---
name: Clinical Precision AI
colors:
  surface: '#f8f9ff'
  surface-dim: '#cbdbf5'
  surface-bright: '#f8f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#eff4ff'
  surface-container: '#e5eeff'
  surface-container-high: '#dce9ff'
  surface-container-highest: '#d3e4fe'
  on-surface: '#0b1c30'
  on-surface-variant: '#45464d'
  inverse-surface: '#213145'
  inverse-on-surface: '#eaf1ff'
  outline: '#76777d'
  outline-variant: '#c6c6cd'
  surface-tint: '#565e74'
  primary: '#000000'
  on-primary: '#ffffff'
  primary-container: '#131b2e'
  on-primary-container: '#7c839b'
  inverse-primary: '#bec6e0'
  secondary: '#006a61'
  on-secondary: '#ffffff'
  secondary-container: '#86f2e4'
  on-secondary-container: '#006f66'
  tertiary: '#000000'
  on-tertiary: '#ffffff'
  tertiary-container: '#001d31'
  on-tertiary-container: '#188ace'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#dae2fd'
  primary-fixed-dim: '#bec6e0'
  on-primary-fixed: '#131b2e'
  on-primary-fixed-variant: '#3f465c'
  secondary-fixed: '#89f5e7'
  secondary-fixed-dim: '#6bd8cb'
  on-secondary-fixed: '#00201d'
  on-secondary-fixed-variant: '#005049'
  tertiary-fixed: '#cce5ff'
  tertiary-fixed-dim: '#93ccff'
  on-tertiary-fixed: '#001d31'
  on-tertiary-fixed-variant: '#004b73'
  background: '#f8f9ff'
  on-background: '#0b1c30'
  surface-variant: '#d3e4fe'
typography:
  display-lg:
    fontFamily: Manrope
    fontSize: 3rem
    fontWeight: '700'
    lineHeight: 3.5rem
    letterSpacing: -0.025em
  headline-lg:
    fontFamily: Manrope
    fontSize: 2rem
    fontWeight: '700'
    lineHeight: 2.5rem
    letterSpacing: -0.02em
  headline-lg-mobile:
    fontFamily: Manrope
    fontSize: 1.5rem
    fontWeight: '700'
    lineHeight: 2rem
    letterSpacing: -0.015em
  headline-md:
    fontFamily: Manrope
    fontSize: 1.375rem
    fontWeight: '600'
    lineHeight: 1.75rem
    letterSpacing: -0.01em
  title-md:
    fontFamily: Inter
    fontSize: 1rem
    fontWeight: '600'
    lineHeight: 1.5rem
  body-lg:
    fontFamily: Inter
    fontSize: 1rem
    fontWeight: '400'
    lineHeight: 1.5rem
  body-md:
    fontFamily: Inter
    fontSize: 0.875rem
    fontWeight: '400'
    lineHeight: 1.375rem
  body-sm:
    fontFamily: Inter
    fontSize: 0.75rem
    fontWeight: '400'
    lineHeight: 1.125rem
  metric-display:
    fontFamily: Manrope
    fontSize: 2.25rem
    fontWeight: '700'
    lineHeight: 2.5rem
    letterSpacing: -0.02em
  label-md:
    fontFamily: Inter
    fontSize: 0.8125rem
    fontWeight: '500'
    lineHeight: 1.125rem
  label-sm:
    fontFamily: Inter
    fontSize: 0.6875rem
    fontWeight: '600'
    lineHeight: 0.875rem
    letterSpacing: 0.05em
  data-mono:
    fontFamily: JetBrains Mono
    fontSize: 0.8125rem
    fontWeight: '500'
    lineHeight: 1.125rem
rounded:
  sm: 0.125rem
  DEFAULT: 0.25rem
  md: 0.375rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  gutter: 1rem
  gutter-md: 1.5rem
  gutter-lg: 1.5rem
  margin: 1rem
  margin-md: 1.5rem
  margin-lg: 2rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 0.875rem
  space-lg: 1.25rem
  space-xl: 2rem
---

## Brand & Style

This design system delivers an authoritative, clinical-grade interface engineered for cardiologists, triage clinicians, and diagnostic specialists. The visual narrative bridges medical rigor with modern computational intelligence. The experience eliminates cognitive drag and diagnostic fatigue through calculated restraint, high-clarity typography, and high-visibility risk categorization.

The design movement combines **Minimalism** and **Modern Clinical Corporate**. It prioritizes dense, unambiguous data delivery over decoration:
- **Clinical Confidence:** High-contrast typographic scales and deliberate framing establish immediate diagnostic credibility.
- **Cognitive Clarity:** Utilitarian layouts isolate life-critical risk variables, preventing interpretation errors during rapid clinical evaluation.
- **Calibrated Feedback:** Color is reserved strictly for semantic states (triage flags, thresholds, telemetry updates) against a neutral slate backdrop.

## Colors

The palette employs a cool, sterile canvas with purposeful structural framing and deterministic semantic signaling.

### Canvas & Structural Pigments
- **App Canvas:** `#F8FAFC` to `#F0F6FA` provides a cool, anti-glare environment optimized for long diagnostic sessions.
- **Card Surfaces:** `#FFFFFF` pure white delivers clinical contrast against the pale blue-gray substrate.
- **Structural Chrome:** `#0F172A` (deep navy) and `#1E293B` (slate navy) anchor navigational rails, toolbars, and primary execution triggers.
- **Neutral Boundaries:** `#E2E8F0` for structural division lines and hairline perimeter strokes; `#64748B` for secondary descriptive metadata.

### Clinical & Algorithmic Accents
- **Clinical Teal (`#0D9488`):** AI confidence indicators, active diagnostic states, and computational confirmations.
- **Precision Cyan (`#0284C7` / `#0EA5E9`):** Interactive selections, active focus rings, and secondary analytical toggles.

### Semantic Triage Tiers
Color application for diagnostic risk is absolute and immutable across the interface:
- **Low Risk (Optimal):** `#10B981` (primary fill/accent) / `#059669` (text/border on light background) with `#ECFDF5` background tints.
- **Moderate Risk (Warning/Borderline):** `#F59E0B` / `#D97706` with `#FFFBEB` background tints.
- **High Risk (Critical Path):** `#EF4444` / `#DC2626` with `#FEF2F2` background tints.

## Typography

The typographic hierarchy pairs **Manrope** for authoritative structural framing and quantitative displays with **Inter** for dense clinical legibility. **JetBrains Mono** is introduced for raw dataset variable names (e.g., `trestbps`, `chol`, `thalach`, `ca`), physiological units, and telemetry logs.

All tabular data sets, metrics, and vital displays must enforce OpenType tabular figures (`font-variant-numeric: tabular-nums`) to ensure vertical alignment of rapidly changing digits in monitoring views and confusion matrices.

## Layout & Spacing

The layout uses a **fluid-grid architecture** bound to strict content containers to preserve density across surgical tablets, diagnostic workstations, and clinical mobile carts.

### Grid & Breakpoints
- **Mobile (< 768px):** 4-column layout, `margin`: `1rem`, `gutter`: `1rem`. Input matrices collapse into linear vertical step flows.
- **Tablet (768px – 1024px):** 8-column layout, `margin`: `1.5rem`, `gutter`: `1.5rem`. Navigational rail collapses to icon-only state; dual metrics share rows.
- **Desktop (1025px+):** 12-column layout with a fixed `16rem` navigation drawer, `margin`: `2rem`, `gutter`: `1.5rem`. Content region supports side-by-side patient evaluation: input parameters on the left (7 columns), predictive outcomes and risk gauges on the right (5 columns).

### Rhythm
Components follow an uncompromising 4px/8px micro-grid. Clinical density demands tight vertical containment: data table row heights are clamped at `44px`, form fields at `40px`, and status badges at `24px` to minimize scrolling during patient evaluations.

## Elevation & Depth

This design system rejects deep drop shadows, heavy blur filters, and skeuomorphic gradients in favor of **low-contrast outlines, micro-depth, and tonal layering**.

- **Level 0 (App Canvas):** Flat `#F8FAFC` base substrate.
- **Level 1 (Clinical Cards & Tables):** Surface `#FFFFFF` enclosed by a 1px border (`#E2E8F0`). Flat elevation with a subtle ambient tint shadow: `0 1px 2px 0 rgba(15, 23, 42, 0.04)`.
- **Level 2 (Interactive Flyouts, Tooltips & Variable Hints):** Surface `#FFFFFF` with 1px border (`#CBD5E1`) and directional elevation: `0 4px 6px -1px rgba(15, 23, 42, 0.08), 0 2px 4px -2px rgba(15, 23, 42, 0.04)`.
- **Level 3 (Modal Alerts & Critical Triage Overlays):** Surface `#FFFFFF` with 1px border (`#94A3B8`) and high-focus containment: `0 20px 25px -5px rgba(15, 23, 42, 0.12), 0 8px 10px -6px rgba(15, 23, 42, 0.06)`.

Surfaces never intersect without a boundary line; physical borders take precedence over shadow cues to maintain edge visibility on uncalibrated hospital LCD panels.

## Shapes

The interface implements **Soft Roundedness (Level 1)**. This geometry balances modern software tactility with clinical seriousness, rejecting rounded "bubble" aesthetics that diminish institutional authority.

- **Inputs, Buttons, Badges, Table Rows:** `0.25rem` (4px).
- **Cards, Confusion Matrix Blocks, Diagnostic Gauges:** `0.5rem` (8px).
- **Dialogs & Clinical Modals:** `0.75rem` (12px).
- **Circular Indicators:** `9999px` reserved strictly for status light pips, toggle switches, and gauge rings.

## Components

### 1. Primary Action & Diagnostic Buttons
- **Primary Execution:** Solid `#0F172A` background, `#FFFFFF` text, `0.25rem` radius, `40px` height. Hover shifts to `#1E293B`. Focus displays a 2px offset ring in `#0284C7`.
- **AI Secondary Action:** Border 1px `#0D9488`, background transparent, text `#0D9488`. Hover shifts to `#F0FDFA`.
- **Destructive/Emergency:** Background `#EF4444`, text `#FFFFFF`. Hover shifts to `#DC2626`.

### 2. High-Precision Metric Cards
- White `#FFFFFF` card bounded by 1px `#E2E8F0` border.
- Header displays metric label (`label-sm`, uppercase, `#64748B`) paired with an informational icon triggering variable definitions.
- Central quantitative value rendered in `metric-display` with embedded tabular numbers and clinical units (`label-md`, `#94A3B8`, e.g., `mg/dL`, `mm Hg`).
- Sub-text indicates algorithmic percentile or comparison against nominal baseline.

### 3. Risk Gauge Visualizations
- Arc or linear segmented progress tracks with 180-degree or horizontal orientation.
- Background trough `#F1F5F9`. Segments utilize exact risk color stops:
  - Low (0–30%): `#10B981`
  - Medium (31–70%): `#F59E0B`
  - High (71–100%): `#EF4444`
- Center read-out presents calculated percentage alongside a bold patient status badge.

### 4. Patient Status Badges & Chips
- Compact `24px` height, `0.25rem` corner radius, `label-sm` font, uppercase.
- **Low Risk:** `#ECFDF5` background, `#047857` text, `#A7F3D0` 1px border.
- **Medium Risk:** `#FFFBEB` background, `#B45309` text, `#FDE68A` 1px border.
- **High Risk:** `#FEF2F2` background, `#B91C1C` text, `#FECACA` 1px border.
- Optional 6px circular pip positioned left of text to guarantee accessibility under monochromatic view conditions.

### 5. Multi-Step Tabbed Input Forms & Clinical Variable Hints
- Persistent step indicators along top edge: active tab signaled by deep navy bottom border (2px) and `#0F172A` text; inactive tabs use `#64748B`.
- Field containers feature floating labels paired with technical variable identifiers styled in `data-mono` (e.g., `Resting Blood Pressure [trestbps]`).
- Hover or keyboard focus on fields reveals an inline, non-blocking metadata card providing nominal reference ranges (e.g., `Normal: < 120 mm Hg`).

### 6. Confusion Matrix & Clinical Data Tables
- Header row: `#F8FAFC` background, `0.75rem` uppercase label font, 1px `#E2E8F0` bottom border.
- Cell rows: `#FFFFFF` base, alternating subtle row background on hover (`#F1F5F9`). Row height pinned to `44px`.
- Confusion Matrix cells map prediction outcomes (True Positive, False Positive, True Negative, False Negative) using dual-tint backgrounds: correct predictions use tinted teal/emerald surfaces (`#F0FDF4`), classification errors use tinted red surfaces (`#FEF2F2`), accompanied by exact sample counts and recall/precision fractions in `data-mono`.