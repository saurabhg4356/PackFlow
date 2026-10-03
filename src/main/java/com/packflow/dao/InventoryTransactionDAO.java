package com.packflow.dao;

import com.packflow.model.InventoryTransaction;
import com.packflow.model.TransactionType;

import java.sql.Connection;
import java.util.List;

/**
 * Data Access Object Interface for Inventory Transactions audit logs.
 */
public interface InventoryTransactionDAO {
    List<InventoryTransaction> findAll();
    List<InventoryTransaction> findByInventoryId(int inventoryId);
    List<InventoryTransaction> findPaginated(int offset, int limit, Integer inventoryId, TransactionType type);
    int countTotal(Integer inventoryId, TransactionType type);
    boolean create(InventoryTransaction transaction);
    boolean create(InventoryTransaction transaction, Connection conn);
}
