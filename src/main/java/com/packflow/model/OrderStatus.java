package com.packflow.model;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Packaging Order Lifecycle Statuses and allowed state transitions.
 * Workflow:
 * PENDING -> APPROVED -> PROCESSING -> QUALITY_CHECK -> COMPLETED -> DISPATCHED -> DELIVERED
 * (Cancellation allowed from PENDING, APPROVED, PROCESSING)
 */
public enum OrderStatus {
    PENDING("Pending Review", "badge bg-secondary"),
    APPROVED("Approved & Reserved", "badge bg-info text-dark"),
    PROCESSING("Packaging In Progress", "badge bg-primary"),
    QUALITY_CHECK("Quality Inspection", "badge bg-warning text-dark"),
    COMPLETED("Packaging Completed", "badge bg-success"),
    DISPATCHED("Dispatched", "badge bg-dark"),
    DELIVERED("Delivered", "badge bg-success"),
    CANCELLED("Cancelled", "badge bg-danger");

    private final String displayName;
    private final String badgeClass;

    OrderStatus(String displayName, String badgeClass) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeClass() {
        return badgeClass;
    }

    /**
     * Determines whether transitioning from this status to next status is permitted.
     *
     * @param next Target order status
     * @return true if valid workflow transition, false otherwise
     */
    public boolean canTransitionTo(OrderStatus next) {
        if (next == null || this == next) {
            return false;
        }

        return switch (this) {
            case PENDING -> next == APPROVED || next == CANCELLED;
            case APPROVED -> next == PROCESSING || next == CANCELLED;
            case PROCESSING -> next == QUALITY_CHECK || next == CANCELLED;
            case QUALITY_CHECK -> next == COMPLETED || next == PROCESSING; // Can send back to processing if QC fails/needs rework
            case COMPLETED -> next == DISPATCHED;
            case DISPATCHED -> next == DELIVERED;
            case DELIVERED, CANCELLED -> false; // Terminal states
        };
    }

    public Set<OrderStatus> getAllowedNextStatuses() {
        return switch (this) {
            case PENDING -> EnumSet.of(APPROVED, CANCELLED);
            case APPROVED -> EnumSet.of(PROCESSING, CANCELLED);
            case PROCESSING -> EnumSet.of(QUALITY_CHECK, CANCELLED);
            case QUALITY_CHECK -> EnumSet.of(COMPLETED, PROCESSING);
            case COMPLETED -> EnumSet.of(DISPATCHED);
            case DISPATCHED -> EnumSet.of(DELIVERED);
            case DELIVERED, CANCELLED -> Collections.emptySet();
        };
    }

    public static OrderStatus fromString(String text) {
        for (OrderStatus s : OrderStatus.values()) {
            if (s.name().equalsIgnoreCase(text)) {
                return s;
            }
        }
        return PENDING;
    }
}
