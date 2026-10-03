package com.packflow.model;

/**
 * Quality Inspection Result Statuses.
 */
public enum QualityStatus {
    PASSED("Passed", "badge bg-success"),
    FAILED("Failed", "badge bg-danger"),
    PARTIAL("Partial Pass / Rework Needed", "badge bg-warning text-dark");

    private final String displayName;
    private final String badgeClass;

    QualityStatus(String displayName, String badgeClass) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeClass() {
        return badgeClass;
    }

    public static QualityStatus fromString(String text) {
        for (QualityStatus s : QualityStatus.values()) {
            if (s.name().equalsIgnoreCase(text)) {
                return s;
            }
        }
        return PASSED;
    }
}
