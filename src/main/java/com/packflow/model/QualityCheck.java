package com.packflow.model;

import java.sql.Timestamp;

/**
 * Quality Inspection Check performed on packaged goods before dispatch approval.
 */
public class QualityCheck {
    private int qualityCheckId;
    private int orderId;
    private Integer checkedBy;
    private int quantityChecked;
    private int quantityPassed;
    private int quantityFailed;
    private String remarks;
    private QualityStatus status;
    private Timestamp checkedAt;

    // Display fields from join
    private String orderNumber;
    private String checkedByName;
    private String companyName;

    public QualityCheck() {
        this.status = QualityStatus.PASSED;
    }

    public QualityCheck(int qualityCheckId, int orderId, Integer checkedBy, int quantityChecked, int quantityPassed, int quantityFailed, String remarks, QualityStatus status, Timestamp checkedAt) {
        this.qualityCheckId = qualityCheckId;
        this.orderId = orderId;
        this.checkedBy = checkedBy;
        this.quantityChecked = quantityChecked;
        this.quantityPassed = quantityPassed;
        this.quantityFailed = quantityFailed;
        this.remarks = remarks;
        this.status = status != null ? status : QualityStatus.PASSED;
        this.checkedAt = checkedAt;
    }

    public int getPassRatePercentage() {
        if (quantityChecked <= 0) return 0;
        return (quantityPassed * 100) / quantityChecked;
    }

    public int getQualityCheckId() {
        return qualityCheckId;
    }

    public void setQualityCheckId(int qualityCheckId) {
        this.qualityCheckId = qualityCheckId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public Integer getCheckedBy() {
        return checkedBy;
    }

    public void setCheckedBy(Integer checkedBy) {
        this.checkedBy = checkedBy;
    }

    public int getQuantityChecked() {
        return quantityChecked;
    }

    public void setQuantityChecked(int quantityChecked) {
        this.quantityChecked = quantityChecked;
    }

    public int getQuantityPassed() {
        return quantityPassed;
    }

    public void setQuantityPassed(int quantityPassed) {
        this.quantityPassed = quantityPassed;
    }

    public int getQuantityFailed() {
        return quantityFailed;
    }

    public void setQuantityFailed(int quantityFailed) {
        this.quantityFailed = quantityFailed;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public QualityStatus getStatus() {
        return status;
    }

    public void setStatus(QualityStatus status) {
        this.status = status;
    }

    public Timestamp getCheckedAt() {
        return checkedAt;
    }

    public void setCheckedAt(Timestamp checkedAt) {
        this.checkedAt = checkedAt;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getCheckedByName() {
        return checkedByName;
    }

    public void setCheckedByName(String checkedByName) {
        this.checkedByName = checkedByName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
}
