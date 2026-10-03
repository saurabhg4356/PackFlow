package com.packflow.service;

import com.packflow.dao.InventoryDAO;
import com.packflow.dao.OrderDAO;
import com.packflow.dao.impl.InventoryDAOImpl;
import com.packflow.dao.impl.OrderDAOImpl;
import com.packflow.exception.InsufficientInventoryException;
import com.packflow.model.InventoryItem;
import com.packflow.model.Order;
import com.packflow.model.OrderItem;
import com.packflow.model.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class OrderWorkflowIntegrationTest {

    @Test
    @DisplayName("Should create order, approve and reserve stock, then cancel and release stock via DB transaction")
    public void testCompleteOrderReservationAndReleaseCycle() throws Exception {
        OrderService orderService = new OrderService();
        InventoryDAO inventoryDAO = new InventoryDAOImpl();
        OrderDAO orderDAO = new OrderDAOImpl();

        // 1. Check initial inventory for Material 1 (Medium Corrugated Box)
        Optional<InventoryItem> optBefore = inventoryDAO.findById(1);
        assertTrue(optBefore.isPresent());
        int initialAvailable = optBefore.get().getQuantityAvailable();
        int initialReserved = optBefore.get().getQuantityReserved();

        // 2. Create test order with Service 1 (Corrugated Box Packing, quantity 10)
        Order order = new Order();
        order.setCustomerId(1);
        order.setNotes("Automated Integration Test Order");

        List<OrderItem> items = new ArrayList<>();
        OrderItem item = new OrderItem();
        item.setProductId(1);
        item.setServiceId(1); // Corrugated Box Packing -> maps to Material 1
        item.setQuantity(10);
        items.add(item);

        Order createdOrder = orderService.createOrder(order, items);
        assertNotNull(createdOrder);
        assertTrue(createdOrder.getOrderId() > 0);
        assertEquals(OrderStatus.PENDING, createdOrder.getStatus());

        // 3. Approve Order & Reserve Inventory (JDBC Transaction)
        orderService.approveOrderAndReserveInventory(createdOrder.getOrderId());

        // Verify status changed to APPROVED
        Optional<Order> optApproved = orderDAO.findById(createdOrder.getOrderId());
        assertTrue(optApproved.isPresent());
        assertEquals(OrderStatus.APPROVED, optApproved.get().getStatus());

        // Verify inventory reserved: available decreased by 10, reserved increased by 10
        Optional<InventoryItem> optAfterApprove = inventoryDAO.findById(1);
        assertTrue(optAfterApprove.isPresent());
        assertEquals(initialAvailable - 10, optAfterApprove.get().getQuantityAvailable());
        assertEquals(initialReserved + 10, optAfterApprove.get().getQuantityReserved());

        // 4. Cancel Order: should release reserved inventory back
        orderService.cancelOrder(createdOrder.getOrderId(), "Integration Test Cleanup");

        Optional<Order> optCancelled = orderDAO.findById(createdOrder.getOrderId());
        assertTrue(optCancelled.isPresent());
        assertEquals(OrderStatus.CANCELLED, optCancelled.get().getStatus());

        // Verify inventory released: available restored, reserved restored
        Optional<InventoryItem> optAfterCancel = inventoryDAO.findById(1);
        assertTrue(optAfterCancel.isPresent());
        assertEquals(initialAvailable, optAfterCancel.get().getQuantityAvailable());
        assertEquals(initialReserved, optAfterCancel.get().getQuantityReserved());
    }

    @Test
    @DisplayName("Should rollback transaction and throw InsufficientInventoryException when required stock exceeds available")
    public void testRejectApprovalWhenInsufficientInventory() throws Exception {
        OrderService orderService = new OrderService();
        InventoryDAO inventoryDAO = new InventoryDAOImpl();

        // Material 7 has 0 available stock in seed data
        // Let's create an order requiring a huge amount of boxes (e.g. 5,000,000)
        Order order = new Order();
        order.setCustomerId(1);
        order.setNotes("Huge quantity order test");

        List<OrderItem> items = new ArrayList<>();
        OrderItem item = new OrderItem();
        item.setProductId(1);
        item.setServiceId(1);
        item.setQuantity(5000000); // 5 Million boxes!
        items.add(item);

        Order created = orderService.createOrder(order, items);

        // Attempt approval
        assertThrows(InsufficientInventoryException.class, () -> {
            orderService.approveOrderAndReserveInventory(created.getOrderId());
        });

        // Verify order remained PENDING and was NOT approved
        Optional<Order> optOrder = orderService.getOrderById(created.getOrderId());
        assertTrue(optOrder.isPresent());
        assertEquals(OrderStatus.PENDING, optOrder.get().getStatus());

        // Cleanup
        orderService.cancelOrder(created.getOrderId(), "Cleanup impossible test order");
    }
}
