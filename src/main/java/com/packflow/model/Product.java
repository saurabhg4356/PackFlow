package com.packflow.model;

import java.sql.Timestamp;

/**
 * Product model representing client items to be packaged.
 */
public class Product {
    private int productId;
    private int customerId;
    private String productName;
    private String productCode;
    private String description;
    private String unit;
    private String status; // 'ACTIVE', 'INACTIVE'
    private Timestamp createdAt;

    // Associated company name from join
    private String companyName;

    public Product() {
        this.unit = "PCS";
        this.status = "ACTIVE";
    }

    public Product(int productId, int customerId, String productName, String productCode, String description, String unit, String status, Timestamp createdAt) {
        this.productId = productId;
        this.customerId = customerId;
        this.productName = productName;
        this.productCode = productCode;
        this.description = description;
        this.unit = unit != null ? unit : "PCS";
        this.status = status != null ? status : "ACTIVE";
        this.createdAt = createdAt;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
}
