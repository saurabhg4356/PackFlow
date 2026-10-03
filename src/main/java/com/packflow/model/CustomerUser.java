package com.packflow.model;

import java.sql.Timestamp;

/**
 * Concrete Customer User subclass demonstrating OOP Inheritance.
 * Customers have restricted privileges and can only access their own orders and requests.
 */
public class CustomerUser extends User {

    private Integer customerId; // References associated customers.customer_id
    private String companyName;

    public CustomerUser() {
        super();
        setRole(Role.CUSTOMER);
    }

    public CustomerUser(int userId, String name, String email, String passwordHash, String status, Timestamp createdAt, Timestamp updatedAt) {
        super(userId, name, email, passwordHash, Role.CUSTOMER, status, createdAt, updatedAt);
    }

    @Override
    public String getRoleDisplayName() {
        return "Customer Client";
    }

    @Override
    public boolean hasAdminPrivileges() {
        return false;
    }

    @Override
    public boolean canManageInventory() {
        return false;
    }

    @Override
    public boolean canManageOrders() {
        return false;
    }

    @Override
    public boolean canPerformQualityCheck() {
        return false;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
}
