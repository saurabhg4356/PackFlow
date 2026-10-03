package com.packflow.model;

import java.sql.Timestamp;

/**
 * Abstract Base Class representing a PackFlow System User.
 * Demonstrates OOP Abstraction, Encapsulation, and Polymorphism.
 */
public abstract class User {
    private int userId;
    private String name;
    private String email;
    private String passwordHash;
    private Role role;
    private String status; // 'ACTIVE', 'INACTIVE'
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Default Constructor
    protected User() {
    }

    // Parameterized Constructor
    protected User(int userId, String name, String email, String passwordHash, Role role, String status, Timestamp createdAt, Timestamp updatedAt) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.status = status != null ? status : "ACTIVE";
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Abstract methods demonstrating Polymorphism
    public abstract String getRoleDisplayName();
    public abstract boolean hasAdminPrivileges();
    public abstract boolean canManageInventory();
    public abstract boolean canManageOrders();
    public abstract boolean canPerformQualityCheck();

    /**
     * Factory method creating the appropriate concrete subclass based on Role.
     */
    public static User create(int userId, String name, String email, String passwordHash, Role role, String status, Timestamp createdAt, Timestamp updatedAt) {
        if (role == null) {
            role = Role.CUSTOMER;
        }
        return switch (role) {
            case ADMIN -> new Admin(userId, name, email, passwordHash, status, createdAt, updatedAt);
            case MANAGER -> new Manager(userId, name, email, passwordHash, status, createdAt, updatedAt);
            case CUSTOMER -> new CustomerUser(userId, name, email, passwordHash, status, createdAt, updatedAt);
        };
    }

    // Encapsulation: Standard Getters & Setters
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
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

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(this.status);
    }
}
