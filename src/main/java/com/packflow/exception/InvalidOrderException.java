package com.packflow.exception;

/**
 * Thrown when an order contains invalid items, negative quantities, or missing mandatory associations.
 */
public class InvalidOrderException extends Exception {
    public InvalidOrderException(String message) {
        super(message);
    }
}
