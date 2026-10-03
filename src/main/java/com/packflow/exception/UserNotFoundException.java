package com.packflow.exception;

/**
 * Thrown when a specified user cannot be located by ID or email credentials.
 */
public class UserNotFoundException extends Exception {
    public UserNotFoundException(String message) {
        super(message);
    }
}
