package com.packflow.model;

import java.sql.Timestamp;

/**
 * Packaging Task assigned to operational staff or manager to assemble and package an order.
 */
public class PackagingTask {
    private int taskId;
    private int orderId;
    private Integer assignedTo;
    private int quantity;
    private int completedQuantity;
    private TaskStatus status;
    private Timestamp startedAt;
    private Timestamp completedAt;

    // Display fields from join
    private String orderNumber;
    private String assignedToName;
    private String companyName;

    public PackagingTask() {
        this.status = TaskStatus.PENDING;
    }

    public PackagingTask(int taskId, int orderId, Integer assignedTo, int quantity, int completedQuantity, TaskStatus status, Timestamp startedAt, Timestamp completedAt) {
        this.taskId = taskId;
        this.orderId = orderId;
        this.assignedTo = assignedTo;
        this.quantity = quantity;
        this.completedQuantity = completedQuantity;
        this.status = status != null ? status : TaskStatus.PENDING;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
    }

    public int getProgressPercentage() {
        if (quantity <= 0) return 0;
        int pct = (completedQuantity * 100) / quantity;
        return Math.min(pct, 100);
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public Integer getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(Integer assignedTo) {
        this.assignedTo = assignedTo;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getCompletedQuantity() {
        return completedQuantity;
    }

    public void setCompletedQuantity(int completedQuantity) {
        this.completedQuantity = completedQuantity;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Timestamp getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Timestamp startedAt) {
        this.startedAt = startedAt;
    }

    public Timestamp getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Timestamp completedAt) {
        this.completedAt = completedAt;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getAssignedToName() {
        return assignedToName;
    }

    public void setAssignedToName(String assignedToName) {
        this.assignedToName = assignedToName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
}
