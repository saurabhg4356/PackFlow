package com.packflow.util;

import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import static org.junit.jupiter.api.Assertions.*;

public class DBConnectionTest {

    @Test
    public void testGetConnectionAndQuery() {
        assertDoesNotThrow(() -> {
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM users");
                 ResultSet rs = stmt.executeQuery()) {
                assertTrue(rs.next());
                int count = rs.getInt(1);
                assertTrue(count >= 5, "Users count should be at least 5 from seed data, was " + count);
            }
        });
    }
}
