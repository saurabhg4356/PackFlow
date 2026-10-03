package com.packflow.model;

import java.sql.Timestamp;

/**
 * Concrete Admin User subclass demonstrating OOP Inheritance.
 */
public class Admin extends User {

    public Admin() {
        super();
        setRole(Role.ADMIN);
    }

    public Admin(int userId, String name, String email, String passwordHash, String status, Timestamp createdAt, Timestamp updatedAt) {
        super(userId, name, email, passwordHash, Role.ADMIN, status, createdAt, updatedAt);
    }

    @Override
    public String getRoleDisplayName() {
        return "System Administrator";
    }

    @Override
    public boolean hasAdminPrivileges() {
        return true;
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
