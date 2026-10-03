package com.packflow.exception;

/**
 * Thrown when an authenticated user attempts to access a protected resource without required role privileges.
 */
public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
