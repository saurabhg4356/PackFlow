package com.packflow.dao;

import com.packflow.model.InventoryItem;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object Interface for Inventory Management and Stock Reservations.
 */
public interface InventoryDAO {
    Optional<InventoryItem> findById(int inventoryId);
    Optional<InventoryItem> findById(int inventoryId, Connection conn);
    Optional<InventoryItem> findByCode(String materialCode);
    List<InventoryItem> findAll();
    List<InventoryItem> findLowStock();
    List<InventoryItem> findPaginated(int offset, int limit, String search, String stockFilter);
    int countTotal(String search, String stockFilter);

    boolean create(InventoryItem item);
    boolean update(InventoryItem item);

    // Atomic transaction methods
    boolean reserveStock(int inventoryId, int quantity, Connection conn);
    boolean releaseStock(int inventoryId, int quantity, Connection conn);
    boolean consumeStock(int inventoryId, int quantity, Connection conn);
    boolean adjustStock(int inventoryId, int newAvailable, Connection conn);

    boolean delete(int inventoryId);
}
