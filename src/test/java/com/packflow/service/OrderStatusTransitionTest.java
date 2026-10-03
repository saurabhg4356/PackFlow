package com.packflow.service;

import com.packflow.exception.InvalidStatusTransitionException;
import com.packflow.model.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OrderStatusTransitionTest {

    @Test
    @DisplayName("Valid workflow transitions should return true")
    public void testValidTransitions() {
        assertTrue(OrderStatus.PENDING.canTransitionTo(OrderStatus.APPROVED));
        assertTrue(OrderStatus.PENDING.canTransitionTo(OrderStatus.CANCELLED));

        assertTrue(OrderStatus.APPROVED.canTransitionTo(OrderStatus.PROCESSING));
        assertTrue(OrderStatus.APPROVED.canTransitionTo(OrderStatus.CANCELLED));

        assertTrue(OrderStatus.PROCESSING.canTransitionTo(OrderStatus.QUALITY_CHECK));
        assertTrue(OrderStatus.PROCESSING.canTransitionTo(OrderStatus.CANCELLED));

        assertTrue(OrderStatus.QUALITY_CHECK.canTransitionTo(OrderStatus.COMPLETED));
        assertTrue(OrderStatus.QUALITY_CHECK.canTransitionTo(OrderStatus.PROCESSING)); // Rework

        assertTrue(OrderStatus.COMPLETED.canTransitionTo(OrderStatus.DISPATCHED));
        assertTrue(OrderStatus.DISPATCHED.canTransitionTo(OrderStatus.DELIVERED));
    }

    @Test
    @DisplayName("Invalid workflow jumps should return false")
    public void testInvalidTransitions() {
        assertFalse(OrderStatus.PENDING.canTransitionTo(OrderStatus.PROCESSING));
        assertFalse(OrderStatus.PENDING.canTransitionTo(OrderStatus.DELIVERED));
        assertFalse(OrderStatus.APPROVED.canTransitionTo(OrderStatus.DELIVERED));
        assertFalse(OrderStatus.COMPLETED.canTransitionTo(OrderStatus.PENDING));
        assertFalse(OrderStatus.DELIVERED.canTransitionTo(OrderStatus.CANCELLED));
        assertFalse(OrderStatus.CANCELLED.canTransitionTo(OrderStatus.APPROVED));
        assertFalse(OrderStatus.PENDING.canTransitionTo(OrderStatus.PENDING)); // Same status
    }

    @Test
    @DisplayName("Invalid transition exception should format message properly")
    public void testInvalidTransitionExceptionFormatting() {
        InvalidStatusTransitionException ex = new InvalidStatusTransitionException("PENDING", "DELIVERED");
        assertEquals("PENDING", ex.getCurrentStatus());
        assertEquals("DELIVERED", ex.getRequestedStatus());
        assertTrue(ex.getMessage().contains("from 'PENDING' to 'DELIVERED'"));
    }
}
