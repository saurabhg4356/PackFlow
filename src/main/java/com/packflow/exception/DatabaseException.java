package com.packflow.exception;

/**
 * Thrown when an underlying database access, SQL execution, or connection failure occurs.
 */
public class DatabaseException extends RuntimeException {
    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
