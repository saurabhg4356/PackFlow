package com.packflow.util;

import com.packflow.exception.DatabaseException;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread-safe Database Connection Utility managing JDBC Connections
 * configured via external db.properties.
 */
public class DBConnection {

    private static final Logger LOGGER = Logger.getLogger(DBConnection.class.getName());
    private static final Properties PROPERTIES = new Properties();

    private static String url;
    private static String username;
    private static String password;
    private static String driver;

    static {
        loadProperties();
    }

    private DBConnection() {
        // Prevent direct instantiation
    }

    private static synchronized void loadProperties() {
        try (InputStream is = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (is != null) {
                PROPERTIES.load(is);
                LOGGER.info("Successfully loaded db.properties configuration.");
            } else {
                LOGGER.warning("db.properties not found in classpath. Attempting db.properties.example fallback.");
                try (InputStream fallbackIs = DBConnection.class.getClassLoader().getResourceAsStream("db.properties.example")) {
                    if (fallbackIs != null) {
                        PROPERTIES.load(fallbackIs);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load database properties", e);
        }

        driver = PROPERTIES.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
        url = PROPERTIES.getProperty("db.url", "jdbc:mysql://localhost:3307/packflow_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        username = PROPERTIES.getProperty("db.username", "root");
        password = PROPERTIES.getProperty("db.password", "");

        try {
            Class.forName(driver);
            LOGGER.info("JDBC Driver initialized: " + driver);
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "MySQL JDBC Driver not found in classpath: " + driver, e);
            throw new DatabaseException("MySQL JDBC Driver not found: " + driver, e);
        }
    }

    /**
     * Obtains a new database connection from the driver.
     *
     * @return active java.sql.Connection instance
     * @throws DatabaseException if connection fails
     */
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to connect to database at: " + url, e);
            throw new DatabaseException("Could not connect to database: " + e.getMessage(), e);
        }
    }

    /**
     * Safely closes a database Connection.
     */
    public static void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing Connection", e);
            }
        }
    }

    /**
     * Safely closes a Statement.
     */
    public static void close(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing Statement", e);
            }
        }
    }

    /**
     * Safely closes a ResultSet.
     */
    public static void close(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing ResultSet", e);
            }
        }
    }

    /**
     * Safely closes ResultSet, Statement, and Connection together.
     */
    public static void close(ResultSet rs, Statement stmt, Connection conn) {
        close(rs);
        close(stmt);
        close(conn);
    }

    /**
     * Safely rolls back a transaction if active.
     */
    public static void rollback(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
                LOGGER.info("Database transaction rolled back successfully.");
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error during transaction rollback", e);
            }
        }
    }
}
