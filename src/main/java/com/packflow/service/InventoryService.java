package com.packflow.service;

import com.packflow.dao.InventoryDAO;
import com.packflow.dao.InventoryTransactionDAO;
import com.packflow.dao.impl.InventoryDAOImpl;
import com.packflow.dao.impl.InventoryTransactionDAOImpl;
import com.packflow.exception.DatabaseException;
import com.packflow.model.InventoryItem;
import com.packflow.model.InventoryTransaction;
import com.packflow.model.TransactionType;
import com.packflow.util.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Service managing inventory stock levels, restocks, adjustments, and movements.
 */
public class InventoryService {

    private final InventoryDAO inventoryDAO;
    private final InventoryTransactionDAO transactionDAO;

    public InventoryService() {
        this.inventoryDAO = new InventoryDAOImpl();
        this.transactionDAO = new InventoryTransactionDAOImpl();
    }

    public InventoryService(InventoryDAO inventoryDAO, InventoryTransactionDAO transactionDAO) {
        this.inventoryDAO = inventoryDAO;
        this.transactionDAO = transactionDAO;
    }

    public Optional<InventoryItem> getInventoryById(int inventoryId) {
        return inventoryDAO.findById(inventoryId);
    }

    public List<InventoryItem> getAllInventory() {
        return inventoryDAO.findAll();
    }

    public List<InventoryItem> getLowStockItems() {
        return inventoryDAO.findLowStock();
    }

    public List<InventoryItem> getPaginatedInventory(int page, int pageSize, String search, String stockFilter) {
        int offset = Math.max(0, (page - 1) * pageSize);
        return inventoryDAO.findPaginated(offset, pageSize, search, stockFilter);
    }

    public int getTotalInventoryCount(String search, String stockFilter) {
        return inventoryDAO.countTotal(search, stockFilter);
    }

    public boolean saveInventory(InventoryItem item) {
        if (item.getMaterialName() == null || item.getMaterialName().trim().isEmpty()) {
            throw new IllegalArgumentException("Material name is required.");
        }
        if (item.getMaterialCode() == null || item.getMaterialCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Material code is required.");
        }

        if (item.getInventoryId() > 0) {
            return inventoryDAO.update(item);
        } else {
            return inventoryDAO.create(item);
        }
    }

    /**
     * Restocks packaging material using a database transaction.
     */
    public boolean restockMaterial(int inventoryId, int addQuantity, String referenceType, Integer referenceId) {
        if (addQuantity <= 0) {
            throw new IllegalArgumentException("Restock quantity must be greater than zero.");
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            Optional<InventoryItem> optItem = inventoryDAO.findById(inventoryId, conn);
            if (optItem.isEmpty()) {
                throw new IllegalArgumentException("Inventory item not found: " + inventoryId);
            }

            InventoryItem item = optItem.get();
            int newAvailable = item.getQuantityAvailable() + addQuantity;
            inventoryDAO.adjustStock(inventoryId, newAvailable, conn);

            InventoryTransaction tx = new InventoryTransaction();
            tx.setInventoryId(inventoryId);
            tx.setTransactionType(TransactionType.PURCHASE);
            tx.setQuantity(addQuantity);
            tx.setReferenceType(referenceType != null ? referenceType : "PURCHASE_RECEIPT");
            tx.setReferenceId(referenceId);
            transactionDAO.create(tx, conn);

            conn.commit();
            return true;
        } catch (Exception e) {
            DBConnection.rollback(conn);
            throw new DatabaseException("Failed to restock inventory item ID: " + inventoryId, e);
        } finally {
            DBConnection.close(conn);
        }
    }

    /**
     * Manually adjusts stock level with audit logging.
     */
    public boolean adjustStock(int inventoryId, int newAvailable, String reason) {
        if (newAvailable < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative.");
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            Optional<InventoryItem> optItem = inventoryDAO.findById(inventoryId, conn);
            if (optItem.isEmpty()) {
                throw new IllegalArgumentException("Inventory item not found: " + inventoryId);
            }

            InventoryItem item = optItem.get();
            int diff = newAvailable - item.getQuantityAvailable();
            inventoryDAO.adjustStock(inventoryId, newAvailable, conn);

            InventoryTransaction tx = new InventoryTransaction();
            tx.setInventoryId(inventoryId);
            tx.setTransactionType(TransactionType.ADJUSTMENT);
            tx.setQuantity(Math.abs(diff));
            tx.setReferenceType("MANUAL_ADJUST: " + (reason != null ? reason : "Inventory Audit"));
            tx.setReferenceId(inventoryId);
            transactionDAO.create(tx, conn);

            conn.commit();
            return true;
        } catch (Exception e) {
            DBConnection.rollback(conn);
            throw new DatabaseException("Failed to adjust inventory stock ID: " + inventoryId, e);
        } finally {
            DBConnection.close(conn);
        }
    }

    public List<InventoryTransaction> getPaginatedTransactions(int page, int pageSize, Integer inventoryId, TransactionType type) {
        int offset = Math.max(0, (page - 1) * pageSize);
        return transactionDAO.findPaginated(offset, pageSize, inventoryId, type);
    }

    public int getTotalTransactionCount(Integer inventoryId, TransactionType type) {
        return transactionDAO.countTotal(inventoryId, type);
    }

    public boolean deleteInventory(int inventoryId) {
        return inventoryDAO.delete(inventoryId);
    }
}
