package com.packflow.util;

import org.mindrot.jbcrypt.BCrypt;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

/**
 * Utility for hashing and verifying user passwords using BCrypt.
 * Includes fallback verification for SHA-256 for maximum compatibility.
 */
public class PasswordUtil {

    private PasswordUtil() {
        // Prevent instantiation
    }

    /**
     * Hashes a plain text password using BCrypt with salt factor 10.
     *
     * @param plainPassword The plain text password
     * @return The hashed password string
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
    }

    /**
     * Verifies a plain text password against a stored hashed password.
     *
     * @param plainPassword The plain text password provided by the user
     * @param hashedPassword The hash stored in the database
     * @return true if matches, false otherwise
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null || plainPassword.isEmpty() || hashedPassword.isEmpty()) {
            return false;
        }

        // Standard BCrypt check
        if (hashedPassword.startsWith("$2a$") || hashedPassword.startsWith("$2b$") || hashedPassword.startsWith("$2y$")) {
            try {
                return BCrypt.checkpw(plainPassword, hashedPassword);
            } catch (Exception e) {
                return false;
            }
        }

        // Fallback for SHA-256 hexadecimal hash
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            if (sb.toString().equalsIgnoreCase(hashedPassword)) {
                return true;
            }
        } catch (Exception ignored) {
        }

        // Fallback for plain text comparison (useful for testing/dev environments only)
        return plainPassword.equals(hashedPassword);
    }
}
