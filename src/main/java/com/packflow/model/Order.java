package com.packflow.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Packaging Order entity encapsulating line items, financial totals,
 * customer associations, and operational workflow status.
 */
public class Order {
    private int orderId;
    private String orderNumber;
    private int customerId;
    private Date orderDate;
    private OrderStatus status;
    private BigDecimal subtotal;
    private BigDecimal totalCost;
    private String notes;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Joined Customer Metadata
    private String companyName;
    private String contactPerson;
    private String customerEmail;
    private String customerPhone;

    // Collections: Natural OOP composition
    private List<OrderItem> items = new ArrayList<>();

    // Associated Workflow Records
    private PackagingTask task;
    private QualityCheck qualityCheck;
    private DispatchRecord dispatch;

    public Order() {
        this.status = OrderStatus.PENDING;
        this.subtotal = BigDecimal.ZERO;
        this.totalCost = BigDecimal.ZERO;
    }

    public Order(int orderId, String orderNumber, int customerId, Date orderDate, OrderStatus status, BigDecimal subtotal, BigDecimal totalCost, String notes, Timestamp createdAt, Timestamp updatedAt) {
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.customerId = customerId;
        this.orderDate = orderDate;
        this.status = status != null ? status : OrderStatus.PENDING;
        this.subtotal = subtotal != null ? subtotal : BigDecimal.ZERO;
        this.totalCost = totalCost != null ? totalCost : BigDecimal.ZERO;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Recalculates subtotal and total cost server-side from line items.
     * Prevents client-side manipulation of monetary values.
     */
    public void recalculateTotals() {
        BigDecimal calculatedSubtotal = BigDecimal.ZERO;
        if (items != null) {
            for (OrderItem item : items) {
                if (item.getTotalPrice() == null || item.getTotalPrice().compareTo(BigDecimal.ZERO) <= 0) {
                    item.calculateTotalPrice();
                }
                calculatedSubtotal = calculatedSubtotal.add(item.getTotalPrice());
            }
        }
        this.subtotal = calculatedSubtotal;
        this.totalCost = calculatedSubtotal; // In future, add taxes or freight if needed
    }

    public void addItem(OrderItem item) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(item);
        recalculateTotals();
    }

    public int getTotalQuantity() {
        if (items == null) return 0;
        int qty = 0;
        for (OrderItem it : items) {
            qty += it.getQuantity();
        }
        return qty;
    }

    // Getters and Setters
    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
        recalculateTotals();
    }

    public PackagingTask getTask() {
        return task;
    }

    public void setTask(PackagingTask task) {
        this.task = task;
    }

    public QualityCheck getQualityCheck() {
        return qualityCheck;
    }

    public void setQualityCheck(QualityCheck qualityCheck) {
        this.qualityCheck = qualityCheck;
    }

    public DispatchRecord getDispatch() {
        return dispatch;
    }

    public void setDispatch(DispatchRecord dispatch) {
        this.dispatch = dispatch;
    }
}
