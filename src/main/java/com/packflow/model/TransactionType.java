package com.packflow.model;

/**
 * Types of inventory movement transactions.
 */
public enum TransactionType {
    PURCHASE("Purchase / Restock", "text-success"),
    RESERVATION("Order Reservation", "text-warning"),
    RELEASE("Reservation Release", "text-info"),
    CONSUMPTION("Order Consumption", "text-danger"),
    ADJUSTMENT("Manual Stock Adjustment", "text-secondary");

    private final String displayName;
    private final String textClass;

    TransactionType(String displayName, String textClass) {
        this.displayName = displayName;
        this.textClass = textClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getTextClass() {
        return textClass;
    }

    public static TransactionType fromString(String text) {
        for (TransactionType t : TransactionType.values()) {
            if (t.name().equalsIgnoreCase(text)) {
                return t;
            }
        }
        return ADJUSTMENT;
    }
}
