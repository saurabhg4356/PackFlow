package com.packflow.exception;

/**
 * Thrown when an order workflow status change violates allowed business state transitions.
 */
public class InvalidStatusTransitionException extends Exception {
    private final String currentStatus;
    private final String requestedStatus;

    public InvalidStatusTransitionException(String currentStatus, String requestedStatus) {
        super(String.format("Invalid order status transition from '%s' to '%s'", currentStatus, requestedStatus));
        this.currentStatus = currentStatus;
        this.requestedStatus = requestedStatus;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public String getRequestedStatus() {
        return requestedStatus;
    }
}
