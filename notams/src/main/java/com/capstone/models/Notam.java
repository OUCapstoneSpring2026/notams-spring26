package com.capstone.models;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a single NOTAM (Notice to Air Missions).
 *
 * This is a "model" / "data" class. It intentionally does NOT fetch data from APIs
 * and does NOT parse JSON directly. Another part of the codebase (e.g., CAP-9)
 * will be responsible for parsing the FAA response JSON and constructing {@link Notam}
 * objects from it.
 *
 * Field origins
 * These fields mirror the FAA "coreNOTAMData.notam" object from the sample JSON:
 * id, series, number, type, issued, affectedFIR, selectionCode, traffic, purpose, scope,
 * minimumFL, maximumFL, location, effectiveStart, effectiveEnd, text, classification,
 * accountId, lastUpdated, icaoLocation, coordinates, radius, etc.
 *
 * Why Instant?
 * FAA timestamps are ISO-8601 strings like "2026-02-02T15:22:00.000Z".
 * {@link Instant} is ideal because it precisely represents UTC instants.
 */

/**
 * Immutable model for an FAA NOTAM record.
 * We use a builder because NOTAM payloads contain many optional fields and can
 * vary by NOTAM type/source. The builder keeps object creation readable (vs. a huge
 * constructor) while still enforcing required fields (like id) at build time.
 */

public final class Notam {

    //Stable internal id from the data source (example: "NOTAM_1_79817842").
    private final String id;

    /**
     * NOTAM series (example: "A"). Optional because not all records may include it.
     */
    private final String series;

    // NOTAM number (example: "A0228/26" or "01/047"). 
    private final String number;

    // NOTAM type (example: "N" new, "C" cancel). Stored as raw code for now. 
    private final String type;

    // When the NOTAM was issued/published. 
    private final Instant issued;

    // FIR affected (example: "KZFW" or "ZFW"). Optional. 
    private final String affectedFIR;

    /**
     * ICAO Q-code / selection code (example: "QOBCE", "QPIXX", etc.).
     * Often used later for classification/prioritization.
     */
    private final String selectionCode;

    // Traffic code (example: "IV", "I"). Optional. 
    private final String traffic;

    // Purpose code (example: "M", "NBO"). Optional. 
    private final String purpose;

    // Scope code (example: "AE", "A", "E"). Optional. 
    private final String scope;

    // Minimum flight level as provided (example: "000"). Optional. 
    private final String minimumFL;

    // Maximum flight level as provided (example: "999"). Optional. 
    private final String maximumFL;

    // Location short code (example: "OKC"). Optional. 
    private final String location;

    // Effective start time. 
    private final Instant effectiveStart;

    // Effective end time (required for our project’s filtering/briefing logic). 
    private final Instant effectiveEnd;

    // Free-text NOTAM message body. 
    private final String text;

    // Classification (example: "INTL", "DOM"). Optional. 
    private final String classification;

    // Account id / station (example: "KOKC", "OKC"). Optional. 
    private final String accountId;

    // Last updated timestamp. Optional. 
    private final Instant lastUpdated;

    // ICAO location (example: "KOKC"). Optional. 
    private final String icaoLocation;

    /** Coordinates in compact string form (example: "3524N09736W"). Optional. */
    private final String coordinates;

    /** Radius in NM (?) as provided (example: "005"). Optional. */
    private final String radius;

    /**
     * Optional formatted ICAO translation text (from
     * notamTranslation[0].formattedText).
     * Helpful later for display/debugging.
     */
    private final String formattedText;

    /**
     * Placeholder for prioritization output (CAP-16 / prioritization component).
     * Keep it mutable so scoring can be computed after construction.
     */
    private int importanceScore;

    /**
     * Private constructor used by the {@link Builder}.
     * Ensures required fields are present and normalizes optional fields.
     */
    private Notam(Builder b) {
        /*
         * CHANGED:
         * Replaced the old custom helper methods (requireNonBlank / blankToNull)
         * with normalizeRequired / normalizeOptional.
         * WHY:
         * Brian asked to avoid rolling our own string helper methods and to
         * rely more on standard library behavior. These methods now mainly wrap
         * Java's built-in strip() and isBlank() behavior in a cleaner, more direct way.
         */

        this.id = normalizeRequired(b.id, "id");
        this.number = normalizeRequired(b.number, "number");
        this.issued = Objects.requireNonNull(b.issued, "issued must not be null");
        this.effectiveStart = Objects.requireNonNull(b.effectiveStart, "effectiveStart must not be null");
        this.effectiveEnd = Objects.requireNonNull(b.effectiveEnd, "effectiveEnd must not be null");
        this.text = normalizeRequired(b.text, "text");

        // Optional fields
        this.type = normalizeOptional(b.type);
        this.series = normalizeOptional(b.series);
        this.affectedFIR = normalizeOptional(b.affectedFIR);
        this.selectionCode = normalizeOptional(b.selectionCode);
        this.traffic = normalizeOptional(b.traffic);
        this.purpose = normalizeOptional(b.purpose);
        this.scope = normalizeOptional(b.scope);
        this.minimumFL = normalizeOptional(b.minimumFL);
        this.maximumFL = normalizeOptional(b.maximumFL);
        this.location = normalizeOptional(b.location);
        this.classification = normalizeOptional(b.classification);
        this.accountId = normalizeOptional(b.accountId);
        this.lastUpdated = b.lastUpdated;
        this.icaoLocation = normalizeOptional(b.icaoLocation);
        this.coordinates = normalizeOptional(b.coordinates);
        this.radius = normalizeOptional(b.radius);        this.formattedText = normalizeOptional(b.formattedText);

        this.importanceScore = b.importanceScore;
    }

    /**
     * Creates a new {@link Builder} to construct an immutable {@link Notam}.
     * This is the preferred way to create a Notam object.
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Returns the stable internal id from the data source. */
    public String getId() {
        return id;
    }

    /** Returns the NOTAM series, if present. */
    public Optional<String> getSeries() {
        return Optional.ofNullable(series);
    }

    /** Returns the NOTAM number (human-readable identifier). */
    public String getNumber() {
        return number;
    }

    /** Returns the NOTAM type code (e.g., N, C). */
    public Optional<String> getType() {
        return Optional.ofNullable(type);
    }

    /** Returns the timestamp when the NOTAM was issued/published. */
    public Instant getIssued() {
        return issued;
    }

    /** Returns the affected FIR, if present. */
    public Optional<String> getAffectedFIR() {
        return Optional.ofNullable(affectedFIR);
    }

    /** Returns the selection code / Q-code, if present. */
    public Optional<String> getSelectionCode() {
        return Optional.ofNullable(selectionCode);
    }

    /** Returns the traffic code, if present. */
    public Optional<String> getTraffic() {
        return Optional.ofNullable(traffic);
    }

    /** Returns the purpose code, if present. */
    public Optional<String> getPurpose() {
        return Optional.ofNullable(purpose);
    }

    /** Returns the scope code, if present. */
    public Optional<String> getScope() {
        return Optional.ofNullable(scope);
    }

    /** Returns the minimum flight level string, if present. */
    public Optional<String> getMinimumFL() {
        return Optional.ofNullable(minimumFL);
    }

    /** Returns the maximum flight level string, if present. */
    public Optional<String> getMaximumFL() {
        return Optional.ofNullable(maximumFL);
    }

    /** Returns the location code (often 3-letter), if present. */
    public Optional<String> getLocation() {
        return Optional.ofNullable(location);
    }

    /** Returns the effective start time for the NOTAM. */
    public Instant getEffectiveStart() {
        return effectiveStart;
    }

    /** Returns the effective end time for the NOTAM. */
    public Instant getEffectiveEnd() {
        return effectiveEnd;
    }

    /** Returns the raw free-text body of the NOTAM. */
    public String getText() {
        return text;
    }

    /** Returns the classification code, if present. */
    public Optional<String> getClassification() {
        return Optional.ofNullable(classification);
    }

    /** Returns the account/station id, if present. */
    public Optional<String> getAccountId() {
        return Optional.ofNullable(accountId);
    }

    /** Returns the last updated timestamp, if present. */
    public Optional<Instant> getLastUpdated() {
        return Optional.ofNullable(lastUpdated);
    }

    /** Returns the ICAO location (e.g., KOKC), if present. */
    public Optional<String> getIcaoLocation() {
        return Optional.ofNullable(icaoLocation);
    }

    /** Returns compact coordinate string (e.g., 3524N09736W), if present. */
    public Optional<String> getCoordinates() {
        return Optional.ofNullable(coordinates);
    }

    /** Returns radius string, if present. */
    public Optional<String> getRadius() {
        return Optional.ofNullable(radius);
    }

    /** Returns a human-friendly formatted text version, if present. */
    public Optional<String> getFormattedText() {
        return Optional.ofNullable(formattedText);
    }

    /** Returns the current importance score (default 0 until computed). */
    public int getImportanceScore() {
        return importanceScore;
    }

    /**
     * Sets the importance score after construction.
     * Used by prioritization logic to store computed ranking.
     */
    public void setImportanceScore(int importanceScore) {
        this.importanceScore = importanceScore;
    }

    // Provides a compact string representation useful for debugging logs.
    @Override
    public String toString() {
        return "Notam{" +
                "id='" + id + '\'' +
                ", number='" + number + '\'' +
                ", type='" + type + '\'' +
                ", issued=" + issued +
                ", effectiveStart=" + effectiveStart +
                ", effectiveEnd=" + effectiveEnd +
                ", location=" + location +
                ", selectionCode=" + selectionCode +
                '}';
    }

    /**
     * Equality is based on the stable NOTAM id.
     * This allows lists/sets to avoid duplicates reliably.
     */

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Notam))
            return false;
        Notam notam = (Notam) o;
        return id.equals(notam.id);
    }

    // equals / hashCode are based on the stable unique NOTAM id.

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * CHANGED:
     * This replaces the old requireNonBlank helper
     * WHY:
     * It uses standard Java String behavior (strip + isBlank) instead of a more
     * custom helper-style implementation. This addresses Brian's feedback
     * while keeping validation readable in the constructor.
     */
    private static String normalizeRequired(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        String normalized = value.strip();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }

    /**
     * CHANGED:
     * This replaces the old blankToNull helper.
     * WHY:
     * Same reasoning as above: use standard Java String normalization behavior
     * while still converting blank optional fields to null consistently.
     */
    private static String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.strip();
        return normalized.isBlank() ? null : normalized;
    }

    /**
     * Builder class used to construct an immutable {@link Notam}.
     * Required fields must be set before calling {@link #build()}.
     */
    public static final class Builder {
        private String id;
        private String series;
        private String number;
        private String type;
        private Instant issued;
        private String affectedFIR;
        private String selectionCode;
        private String traffic;
        private String purpose;
        private String scope;
        private String minimumFL;
        private String maximumFL;
        private String location;
        private Instant effectiveStart;
        private Instant effectiveEnd;
        private String text;
        private String classification;
        private String accountId;
        private Instant lastUpdated;
        private String icaoLocation;
        private String coordinates;
        private String radius;
        private String formattedText;
        private int importanceScore = 0;

        /** Private: use {@link Notam#builder()} to start building. */
        private Builder() {
        }

        /** Sets NOTAM id (required). */
        public Builder id(String id) {
            this.id = id;
            return this;
        }

        /** Sets series (optional). */
        public Builder series(String series) {
            this.series = series;
            return this;
        }

        /** Sets number (required). */
        public Builder number(String number) {
            this.number = number;
            return this;
        }

        /** Sets type code (required). */
        public Builder type(String type) {
            this.type = type;
            return this;
        }

        /** Sets issued timestamp (required). */
        public Builder issued(Instant issued) {
            this.issued = issued;
            return this;
        }

        /** Sets affected FIR (optional). */
        public Builder affectedFIR(String affectedFIR) {
            this.affectedFIR = affectedFIR;
            return this;
        }

        /** Sets selection/Q-code (optional). */
        public Builder selectionCode(String selectionCode) {
            this.selectionCode = selectionCode;
            return this;
        }

        /** Sets traffic code (optional). */
        public Builder traffic(String traffic) {
            this.traffic = traffic;
            return this;
        }

        /** Sets purpose code (optional). */
        public Builder purpose(String purpose) {
            this.purpose = purpose;
            return this;
        }

        /** Sets scope code (optional). */
        public Builder scope(String scope) {
            this.scope = scope;
            return this;
        }

        /** Sets minimum flight level (optional). */
        public Builder minimumFL(String minimumFL) {
            this.minimumFL = minimumFL;
            return this;
        }

        /** Sets maximum flight level (optional). */
        public Builder maximumFL(String maximumFL) {
            this.maximumFL = maximumFL;
            return this;
        }

        /** Sets location code (optional). */
        public Builder location(String location) {
            this.location = location;
            return this;
        }

        /** Sets effective start time (required). */
        public Builder effectiveStart(Instant effectiveStart) {
            this.effectiveStart = effectiveStart;
            return this;
        }

        /** Sets effective end time (required). */
        public Builder effectiveEnd(Instant effectiveEnd) {
            this.effectiveEnd = effectiveEnd;
            return this;
        }

        /** Sets NOTAM raw text (required). */
        public Builder text(String text) {
            this.text = text;
            return this;
        }

        /** Sets classification (optional). */
        public Builder classification(String classification) {
            this.classification = classification;
            return this;
        }

        /** Sets account/station id (optional). */
        public Builder accountId(String accountId) {
            this.accountId = accountId;
            return this;
        }

        /** Sets lastUpdated timestamp (optional). */
        public Builder lastUpdated(Instant lastUpdated) {
            this.lastUpdated = lastUpdated;
            return this;
        }

        /** Sets ICAO location code (optional). */
        public Builder icaoLocation(String icaoLocation) {
            this.icaoLocation = icaoLocation;
            return this;
        }

        /** Sets compact coordinate string (optional). */
        public Builder coordinates(String coordinates) {
            this.coordinates = coordinates;
            return this;
        }

        /** Sets radius string (optional). */
        public Builder radius(String radius) {
            this.radius = radius;
            return this;
        }

        /** Sets formatted/translated text (optional). */
        public Builder formattedText(String formattedText) {
            this.formattedText = formattedText;
            return this;
        }

        /** Sets initial importance score (optional; usually computed later). */
        public Builder importanceScore(int importanceScore) {
            this.importanceScore = importanceScore;
            return this;
        }

        /**
         * Builds the {@link Notam}. Validates required fields and normalizes optional
         * fields.
         */
        public Notam build() {
            return new Notam(this);
        }
    }
}