package com.packflow.model;

import java.sql.Timestamp;

/**
 * Audit log record of every inventory movement (Purchase, Reservation, Release, Consumption, Adjustment).
 */
public class InventoryTransaction {
    private int transactionId;
    private int inventoryId;
    private TransactionType transactionType;
    private int quantity;
    private String referenceType; // 'ORDER', 'PURCHASE_ORDER', 'MANUAL_ADJUST'
    private Integer referenceId;
    private Timestamp createdAt;

    // Display fields from join
    private String materialName;
    private String materialCode;
    private String unit;

    public InventoryTransaction() {
    }

    public InventoryTransaction(int transactionId, int inventoryId, TransactionType transactionType, int quantity, String referenceType, Integer referenceId, Timestamp createdAt) {
        this.transactionId = transactionId;
        this.inventoryId = inventoryId;
        this.transactionType = transactionType;
        this.quantity = quantity;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.createdAt = createdAt;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public int getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(int inventoryId) {
        this.inventoryId = inventoryId;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public Integer getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Integer referenceId) {
        this.referenceId = referenceId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
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

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
