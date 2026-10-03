package com.packflow.model;

/**
 * User roles within the PackFlow system.
 */
public enum Role {
    ADMIN("Administrator"),
    MANAGER("Operations Manager"),
    CUSTOMER("Customer / Client");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Role fromString(String text) {
        for (Role r : Role.values()) {
            if (r.name().equalsIgnoreCase(text)) {
                return r;
            }
        }
        return CUSTOMER;
    }
}
