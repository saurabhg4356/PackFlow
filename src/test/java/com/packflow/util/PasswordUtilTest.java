package com.packflow.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PasswordUtilTest {

    @Test
    public void testHashAndVerifyPassword() {
        String password = "admin123";
        String hashed = PasswordUtil.hashPassword(password);

        assertNotNull(hashed);
        assertTrue(hashed.startsWith("$2a$"));
        assertTrue(PasswordUtil.checkPassword(password, hashed));
        assertFalse(PasswordUtil.checkPassword("wrongpassword", hashed));
    }

    @Test
    public void testGenerateSampleHashes() {
        System.out.println("ADMIN HASH: " + PasswordUtil.hashPassword("admin123"));
        System.out.println("MANAGER HASH: " + PasswordUtil.hashPassword("manager123"));
        System.out.println("CUSTOMER HASH: " + PasswordUtil.hashPassword("customer123"));
    }
}
