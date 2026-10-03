package com.packflow.exception;

/**
 * Thrown when attempting to approve an order or reserve materials when stock is insufficient.
 */
public class InsufficientInventoryException extends Exception {
    private final int inventoryId;
    private final String materialName;
    private final int requiredQuantity;
    private final int availableQuantity;

    public InsufficientInventoryException(String message) {
        super(message);
        this.inventoryId = 0;
        this.materialName = "";
        this.requiredQuantity = 0;
        this.availableQuantity = 0;
    }

    public InsufficientInventoryException(int inventoryId, String materialName, int requiredQuantity, int availableQuantity) {
        super(String.format("Insufficient inventory for '%s' (ID: %d). Required: %d, Available: %d",
                materialName, inventoryId, requiredQuantity, availableQuantity));
        this.inventoryId = inventoryId;
        this.materialName = materialName;
        this.requiredQuantity = requiredQuantity;
        this.availableQuantity = availableQuantity;
    }

    public int getInventoryId() {
        return inventoryId;
    }

    public String getMaterialName() {
        return materialName;
    }

    public int getRequiredQuantity() {
        return requiredQuantity;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }
}
