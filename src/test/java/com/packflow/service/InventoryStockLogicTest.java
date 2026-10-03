package com.packflow.service;

import com.packflow.model.InventoryItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InventoryStockLogicTest {

    @Test
    @DisplayName("Should detect healthy inventory when available stock exceeds reorder level")
    public void testHealthyInventory() {
        InventoryItem item = new InventoryItem();
        item.setQuantityAvailable(250);
        item.setQuantityReserved(50);
        item.setReorderLevel(100);

        assertTrue(item.isHealthy());
        assertFalse(item.isLowStock());
        assertFalse(item.isOutOfStock());
        assertEquals("HEALTHY", item.getStockStatus());
        assertEquals(300, item.getTotalPhysicalStock());
    }

    @Test
    @DisplayName("Should detect low stock alert when available stock <= reorder level")
    public void testLowStockAlert() {
        InventoryItem item = new InventoryItem();
        item.setQuantityAvailable(45);
        item.setQuantityReserved(10);
        item.setReorderLevel(50);

        assertFalse(item.isHealthy());
        assertTrue(item.isLowStock());
        assertFalse(item.isOutOfStock());
        assertEquals("LOW_STOCK", item.getStockStatus());
        assertEquals(55, item.getTotalPhysicalStock());
    }

    @Test
    @DisplayName("Should detect out of stock when available quantity is zero")
    public void testOutOfStock() {
        InventoryItem item = new InventoryItem();
        item.setQuantityAvailable(0);
        item.setQuantityReserved(0);
        item.setReorderLevel(20);

        assertFalse(item.isHealthy());
        assertFalse(item.isLowStock());
        assertTrue(item.isOutOfStock());
        assertEquals("OUT_OF_STOCK", item.getStockStatus());
    }
}
