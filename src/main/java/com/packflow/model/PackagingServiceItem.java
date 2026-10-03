package com.packflow.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Packaging Service definition entity (e.g. Box Packing, Pouch Packing, Labeling).
 */
public class PackagingServiceItem {
    private int serviceId;
    private String serviceName;
    private String description;
    private BigDecimal basePrice;
    private String status; // 'ACTIVE', 'INACTIVE'
    private Timestamp createdAt;

    public PackagingServiceItem() {
        this.basePrice = BigDecimal.ZERO;
        this.status = "ACTIVE";
    }

    public PackagingServiceItem(int serviceId, String serviceName, String description, BigDecimal basePrice, String status, Timestamp createdAt) {
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.description = description;
        this.basePrice = basePrice != null ? basePrice : BigDecimal.ZERO;
        this.status = status != null ? status : "ACTIVE";
        this.createdAt = createdAt;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
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
}
