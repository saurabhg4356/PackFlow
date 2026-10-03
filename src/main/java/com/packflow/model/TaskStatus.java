package com.packflow.model;

/**
 * Packaging Task Statuses.
 */
public enum TaskStatus {
    PENDING("Pending Assignment", "badge bg-secondary"),
    IN_PROGRESS("In Progress", "badge bg-primary"),
    COMPLETED("Task Completed", "badge bg-success");

    private final String displayName;
    private final String badgeClass;

    TaskStatus(String displayName, String badgeClass) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeClass() {
        return badgeClass;
    }

    public static TaskStatus fromString(String text) {
        for (TaskStatus s : TaskStatus.values()) {
            if (s.name().equalsIgnoreCase(text)) {
                return s;
            }
        }
        return PENDING;
    }
}
