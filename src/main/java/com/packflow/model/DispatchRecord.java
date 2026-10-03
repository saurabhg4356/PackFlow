package com.packflow.model;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * Dispatch & Delivery Shipment Record.
 */
public class DispatchRecord {
    private int dispatchId;
    private int orderId;
    private Date dispatchDate;
    private String deliveryPartner;
    private String trackingNumber;
    private DispatchStatus status;
    private Timestamp deliveredAt;

    // Display metadata from join
    private String orderNumber;
    private String companyName;
    private String contactPerson;

    public DispatchRecord() {
        this.status = DispatchStatus.IN_TRANSIT;
    }

    public DispatchRecord(int dispatchId, int orderId, Date dispatchDate, String deliveryPartner, String trackingNumber, DispatchStatus status, Timestamp deliveredAt) {
        this.dispatchId = dispatchId;
        this.orderId = orderId;
        this.dispatchDate = dispatchDate;
        this.deliveryPartner = deliveryPartner;
        this.trackingNumber = trackingNumber;
        this.status = status != null ? status : DispatchStatus.IN_TRANSIT;
        this.deliveredAt = deliveredAt;
    }

    public int getDispatchId() {
        return dispatchId;
    }

    public void setDispatchId(int dispatchId) {
        this.dispatchId = dispatchId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public Date getDispatchDate() {
        return dispatchDate;
    }

    public void setDispatchDate(Date dispatchDate) {
        this.dispatchDate = dispatchDate;
    }

    public String getDeliveryPartner() {
        return deliveryPartner;
    }

    public void setDeliveryPartner(String deliveryPartner) {
        this.deliveryPartner = deliveryPartner;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public DispatchStatus getStatus() {
        return status;
    }

    public void setStatus(DispatchStatus status) {
        this.status = status;
    }

    public Timestamp getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(Timestamp deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
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
}
