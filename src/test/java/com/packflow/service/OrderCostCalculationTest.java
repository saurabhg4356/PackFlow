package com.packflow.service;

import com.packflow.exception.InvalidOrderException;
import com.packflow.model.Order;
import com.packflow.model.OrderItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OrderCostCalculationTest {

    private Order order;

    @BeforeEach
    public void setUp() {
        order = new Order();
        order.setCustomerId(1);
    }

    @Test
    @DisplayName("Should correctly calculate line item total price = quantity * unitPrice")
    public void testOrderItemTotalCalculation() {
        OrderItem item = new OrderItem();
        item.setQuantity(50);
        item.setUnitPrice(new BigDecimal("15.50"));

        assertEquals(new BigDecimal("775.00"), item.getTotalPrice());
    }

    @Test
    @DisplayName("Should recalculate order subtotal and total cost from multiple items server-side")
    public void testOrderTotalsRecalculation() {
        OrderItem item1 = new OrderItem();
        item1.setQuantity(100);
        item1.setUnitPrice(new BigDecimal("15.00")); // 1500.00

        OrderItem item2 = new OrderItem();
        item2.setQuantity(200);
        item2.setUnitPrice(new BigDecimal("3.50"));  // 700.00

        order.addItem(item1);
        order.addItem(item2);

        assertEquals(new BigDecimal("2200.00"), order.getSubtotal());
        assertEquals(new BigDecimal("2200.00"), order.getTotalCost());
        assertEquals(300, order.getTotalQuantity());
    }

    @Test
    @DisplayName("Should reject order creation with zero or negative quantity")
    public void testRejectZeroOrNegativeQuantity() {
        OrderService service = new OrderService();
        List<OrderItem> items = new ArrayList<>();

        OrderItem invalidItem = new OrderItem();
        invalidItem.setProductId(1);
        invalidItem.setServiceId(1);
        invalidItem.setQuantity(-5); // Invalid!
        items.add(invalidItem);

        assertThrows(InvalidOrderException.class, () -> {
            service.createOrder(order, items);
        });
    }

    @Test
    @DisplayName("Should reject order creation without any line items")
    public void testRejectEmptyOrderItems() {
        OrderService service = new OrderService();
        List<OrderItem> items = new ArrayList<>();

        assertThrows(InvalidOrderException.class, () -> {
            service.createOrder(order, items);
        });
    }
}
