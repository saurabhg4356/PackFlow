package com.packflow.model;

/**
 * Dispatch and Delivery Shipment Statuses.
 */
public enum DispatchStatus {
    IN_TRANSIT("In Transit", "badge bg-info text-dark"),
    OUT_FOR_DELIVERY("Out for Delivery", "badge bg-warning text-dark"),
    DELIVERED("Delivered", "badge bg-success"),
    RETURNED("Returned", "badge bg-danger");

    private final String displayName;
    private final String badgeClass;

    DispatchStatus(String displayName, String badgeClass) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeClass() {
        return badgeClass;
    }

    public static DispatchStatus fromString(String text) {
        for (DispatchStatus s : DispatchStatus.values()) {
            if (s.name().equalsIgnoreCase(text)) {
                return s;
            }
        }
        return IN_TRANSIT;
    }
}
