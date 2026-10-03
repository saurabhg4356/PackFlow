package com.packflow.model;

import java.math.BigDecimal;

/**
 * Line item within an order specifying product, packaging service, quantity, and prices.
 */
public class OrderItem {
    private int orderItemId;
    private int orderId;
    private int productId;
    private int serviceId;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;

    // Joined display metadata
    private String productName;
    private String productCode;
    private String serviceName;

    public OrderItem() {
        this.unitPrice = BigDecimal.ZERO;
        this.totalPrice = BigDecimal.ZERO;
    }

    public OrderItem(int orderItemId, int orderId, int productId, int serviceId, int quantity, BigDecimal unitPrice, BigDecimal totalPrice) {
        this.orderItemId = orderItemId;
        this.orderId = orderId;
        this.productId = productId;
        this.serviceId = serviceId;
        this.quantity = quantity;
        this.unitPrice = unitPrice != null ? unitPrice : BigDecimal.ZERO;
        this.totalPrice = totalPrice != null ? totalPrice : BigDecimal.ZERO;
    }

    /**
     * Recalculates total price on server side: quantity * unitPrice
     */
    public void calculateTotalPrice() {
        if (this.unitPrice != null && this.quantity > 0) {
            this.totalPrice = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
        } else {
            this.totalPrice = BigDecimal.ZERO;
        }
    }

    public int getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(int orderItemId) {
        this.orderItemId = orderItemId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        calculateTotalPrice();
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        calculateTotalPrice();
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
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

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
