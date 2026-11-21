package com.heartrisk.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The 13 clinical features of the UCI Cleveland heart-disease data set for one patient.
 *
 * <p><b>OOAD: Encapsulation.</b> All fields are private and final; the object is immutable and
 * can only be created through {@link Builder#build()}, which validates every range and throws
 * {@link IllegalArgumentException} for out-of-range or missing values.</p>
 *
 * <p><b>DESIGN PATTERN: Builder.</b> {@link Builder} offers one fluent setter per feature, so
 * callers never have to remember a 13-argument constructor order.</p>
 */
public final class HealthRecord {

    /** Feature names in the fixed order used for CSV columns and for {@link #toFeatureVector()}. */
    public static final List<String> FEATURE_NAMES = List.of(
            "age", "sex", "cp", "trestbps", "chol", "fbs", "restecg",
            "thalach", "exang", "oldpeak", "slope", "ca", "thal");

    private final int age;
    private final int sex;
    private final int cp;
    private final int trestbps;
    private final int chol;
    private final int fbs;
    private final int restecg;
    private final int thalach;
    private final int exang;
    private final double oldpeak;
    private final int slope;
    private final int ca;
    private final int thal;

    private HealthRecord(Builder b) {
        this.age = b.age;
        this.sex = b.sex;
        this.cp = b.cp;
        this.trestbps = b.trestbps;
        this.chol = b.chol;
        this.fbs = b.fbs;
        this.restecg = b.restecg;
        this.thalach = b.thalach;
        this.exang = b.exang;
        this.oldpeak = b.oldpeak;
        this.slope = b.slope;
        this.ca = b.ca;
        this.thal = b.thal;
    }

    /** @return a new, empty builder */
    public static Builder builder() {
        return new Builder();
    }

    /** @return age in years (1-120) */
    public int getAge() { return age; }
    /** @return sex, 0 = female, 1 = male */
    public int getSex() { return sex; }
    /** @return chest-pain type (0-3) */
    public int getCp() { return cp; }
    /** @return resting blood pressure in mm Hg (80-220) */
    public int getTrestbps() { return trestbps; }
    /** @return serum cholesterol in mg/dl (100-600) */
    public int getChol() { return chol; }
    /** @return fasting blood sugar above 120 mg/dl, 0 or 1 */
    public int getFbs() { return fbs; }
    /** @return resting ECG result (0-2) */
    public int getRestecg() { return restecg; }
    /** @return maximum heart rate achieved (60-220) */
    public int getThalach() { return thalach; }
    /** @return exercise-induced angina, 0 or 1 */
    public int getExang() { return exang; }
    /** @return ST depression induced by exercise (0-7) */
    public double getOldpeak() { return oldpeak; }
    /** @return slope of the peak exercise ST segment (0-2) */
    public int getSlope() { return slope; }
    /** @return number of major vessels coloured by fluoroscopy (0-4) */
    public int getCa() { return ca; }
    /** @return thalassemia code (0-3) */
    public int getThal() { return thal; }
    /**
     * Converts the record to the numeric vector consumed by the models.
     *
     * @return 13 values in the order of {@link #FEATURE_NAMES}
     */
    public double[] toFeatureVector() {
        return new double[] {age, sex, cp, trestbps, chol, fbs, restecg,
                thalach, exang, oldpeak, slope, ca, thal};
    }

    /**
     * Converts the record to an ordered name-to-value map (used for JSON and CSV output).
     *
     * @return map in the order of {@link #FEATURE_NAMES}
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        double[] values = toFeatureVector();
        for (int i = 0; i < FEATURE_NAMES.size(); i++) {
            String name = FEATURE_NAMES.get(i);
            map.put(name, name.equals("oldpeak") ? (Object) values[i] : (Object) (int) values[i]);
        }
        return map;
    }

    /** Fluent builder for {@link HealthRecord}; {@link #build()} validates all 13 features. */
    public static final class Builder {
        private Integer age;
        private Integer sex;
        private Integer cp;
        private Integer trestbps;
        private Integer chol;
        private Integer fbs;
        private Integer restecg;
        private Integer thalach;
        private Integer exang;
        private Double oldpeak;
        private Integer slope;
        private Integer ca;
        private Integer thal;

        // One fluent setter per feature; allowed ranges are checked in build().
        public Builder age(int v) { this.age = v; return this; }
        public Builder sex(int v) { this.sex = v; return this; }
        public Builder cp(int v) { this.cp = v; return this; }
        public Builder trestbps(int v) { this.trestbps = v; return this; }
        public Builder chol(int v) { this.chol = v; return this; }
        public Builder fbs(int v) { this.fbs = v; return this; }
        public Builder restecg(int v) { this.restecg = v; return this; }
        public Builder thalach(int v) { this.thalach = v; return this; }
        public Builder exang(int v) { this.exang = v; return this; }
        public Builder oldpeak(double v) { this.oldpeak = v; return this; }
        public Builder slope(int v) { this.slope = v; return this; }
        public Builder ca(int v) { this.ca = v; return this; }
        public Builder thal(int v) { this.thal = v; return this; }

        /**
         * Sets a feature by its column name; used when reading CSV rows and JSON bodies.
         *
         * @param name  one of {@link HealthRecord#FEATURE_NAMES}
         * @param value numeric value; must be a whole number for every feature except oldpeak
         * @return this builder
         * @throws IllegalArgumentException for an unknown name or a fractional whole-number feature
         */
        public Builder feature(String name, double value) {
            return switch (name) {
                case "age" -> age(FeatureValidator.whole(name, value));
                case "sex" -> sex(FeatureValidator.whole(name, value));
                case "cp" -> cp(FeatureValidator.whole(name, value));
                case "trestbps" -> trestbps(FeatureValidator.whole(name, value));
                case "chol" -> chol(FeatureValidator.whole(name, value));
                case "fbs" -> fbs(FeatureValidator.whole(name, value));
                case "restecg" -> restecg(FeatureValidator.whole(name, value));
                case "thalach" -> thalach(FeatureValidator.whole(name, value));
                case "exang" -> exang(FeatureValidator.whole(name, value));
                case "oldpeak" -> oldpeak(value);
                case "slope" -> slope(FeatureValidator.whole(name, value));
                case "ca" -> ca(FeatureValidator.whole(name, value));
                case "thal" -> thal(FeatureValidator.whole(name, value));
                default -> throw new IllegalArgumentException("Unknown feature: " + name);
            };
        }

        /**
         * Validates every feature and creates the immutable record.
         *
         * @return a valid health record
         * @throws IllegalArgumentException if a feature is missing or outside its allowed range
         */
        public HealthRecord build() {
            FeatureValidator.checkRange("age", age, 1, 120);
            FeatureValidator.checkRange("sex", sex, 0, 1);
            FeatureValidator.checkRange("cp", cp, 0, 3);
            FeatureValidator.checkRange("trestbps", trestbps, 80, 220);
            FeatureValidator.checkRange("chol", chol, 100, 600);
            FeatureValidator.checkRange("fbs", fbs, 0, 1);
            FeatureValidator.checkRange("restecg", restecg, 0, 2);
            FeatureValidator.checkRange("thalach", thalach, 60, 220);
            FeatureValidator.checkRange("exang", exang, 0, 1);
            FeatureValidator.checkRange("oldpeak", oldpeak, 0.0, 7.0);
            FeatureValidator.checkRange("slope", slope, 0, 2);
            FeatureValidator.checkRange("ca", ca, 0, 4);
            FeatureValidator.checkRange("thal", thal, 0, 3);
            return new HealthRecord(this);
        }
    }
}
