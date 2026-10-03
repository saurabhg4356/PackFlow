package com.packflow.model;

import java.sql.Timestamp;

/**
 * Concrete Manager User subclass demonstrating OOP Inheritance and Role specialization.
 */
public class Manager extends User {

    public Manager() {
        super();
        setRole(Role.MANAGER);
    }

    public Manager(int userId, String name, String email, String passwordHash, String status, Timestamp createdAt, Timestamp updatedAt) {
        super(userId, name, email, passwordHash, Role.MANAGER, status, createdAt, updatedAt);
    }

    @Override
    public String getRoleDisplayName() {
        return "Operations Manager";
    }

    @Override
    public boolean hasAdminPrivileges() {
        return false;
    }

    @Override
    public boolean canManageInventory() {
        return true;
    }

    @Override
    public boolean canManageOrders() {
        return true;
    }

    @Override
    public boolean canPerformQualityCheck() {
        return true;
    }
}
