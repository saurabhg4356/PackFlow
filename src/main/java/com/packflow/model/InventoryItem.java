package com.packflow.model;

import java.sql.Timestamp;

/**
 * Packaging Material Inventory model.
 * Encapsulates stock levels, reserved quantities, and reorder alerts.
 */
public class InventoryItem {
    private int inventoryId;
    private String materialName;
    private String materialCode;
    private int quantityAvailable;
    private int quantityReserved;
    private int reorderLevel;
    private String unit;
    private Timestamp updatedAt;

    public InventoryItem() {
        this.unit = "PCS";
        this.reorderLevel = 10;
    }

    public InventoryItem(int inventoryId, String materialName, String materialCode, int quantityAvailable, int quantityReserved, int reorderLevel, String unit, Timestamp updatedAt) {
        this.inventoryId = inventoryId;
        this.materialName = materialName;
        this.materialCode = materialCode;
        this.quantityAvailable = quantityAvailable;
        this.quantityReserved = quantityReserved;
        this.reorderLevel = reorderLevel;
        this.unit = unit != null ? unit : "PCS";
        this.updatedAt = updatedAt;
    }

    public boolean isOutOfStock() {
        return quantityAvailable <= 0;
    }

    public boolean isLowStock() {
        return quantityAvailable > 0 && quantityAvailable <= reorderLevel;
    }

    public boolean isHealthy() {
        return quantityAvailable > reorderLevel;
    }

    public int getTotalPhysicalStock() {
        return quantityAvailable + quantityReserved;
    }

    public String getStockStatus() {
        if (isOutOfStock()) return "OUT_OF_STOCK";
        if (isLowStock()) return "LOW_STOCK";
        return "HEALTHY";
    }

    public String getStockStatusBadge() {
        if (isOutOfStock()) {
            return "<span class=\"badge bg-danger\">Out of Stock</span>";
        }
        if (isLowStock()) {
            return "<span class=\"badge bg-warning text-dark\"><i class=\"bi bi-exclamation-triangle-fill me-1\"></i>Low Stock</span>";
        }
        return "<span class=\"badge bg-success\"><i class=\"bi bi-check-circle-fill me-1\"></i>Healthy</span>";
    }

    public int getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(int inventoryId) {
        this.inventoryId = inventoryId;
    }

    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    public String getMaterialCode() {
        return materialCode;
    }

    public void setMaterialCode(String materialCode) {
        this.materialCode = materialCode;
    }

    public int getQuantityAvailable() {
        return quantityAvailable;
    }

    public void setQuantityAvailable(int quantityAvailable) {
        this.quantityAvailable = quantityAvailable;
    }

    public int getQuantityReserved() {
        return quantityReserved;
    }

    public void setQuantityReserved(int quantityReserved) {
        this.quantityReserved = quantityReserved;
    }

    public int getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(int reorderLevel) {
        this.reorderLevel = reorderLevel;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}
