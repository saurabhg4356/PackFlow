package com.packflow.dao.impl;

import com.packflow.dao.InventoryTransactionDAO;
import com.packflow.exception.DatabaseException;
import com.packflow.model.InventoryTransaction;
import com.packflow.model.TransactionType;
import com.packflow.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class InventoryTransactionDAOImpl implements InventoryTransactionDAO {

    private InventoryTransaction mapRow(ResultSet rs) throws SQLException {
        InventoryTransaction t = new InventoryTransaction();
        t.setTransactionId(rs.getInt("transaction_id"));
        t.setInventoryId(rs.getInt("inventory_id"));
        t.setTransactionType(TransactionType.fromString(rs.getString("transaction_type")));
        t.setQuantity(rs.getInt("quantity"));
        t.setReferenceType(rs.getString("reference_type"));
        int refId = rs.getInt("reference_id");
        if (!rs.wasNull()) {
            t.setReferenceId(refId);
        } else {
            t.setReferenceId(null);
        }
        t.setCreatedAt(rs.getTimestamp("created_at"));

        try {
            t.setMaterialName(rs.getString("material_name"));
            t.setMaterialCode(rs.getString("material_code"));
            t.setUnit(rs.getString("unit"));
        } catch (SQLException ignored) {
        }
        return t;
    }

    @Override
    public List<InventoryTransaction> findAll() {
        List<InventoryTransaction> list = new ArrayList<>();
        String sql = "SELECT t.*, i.material_name, i.material_code, i.unit " +
                     "FROM inventory_transactions t " +
                     "JOIN inventory i ON t.inventory_id = i.inventory_id " +
                     "ORDER BY t.transaction_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching inventory transactions", e);
        }
        return list;
    }

    @Override
    public List<InventoryTransaction> findByInventoryId(int inventoryId) {
        List<InventoryTransaction> list = new ArrayList<>();
        String sql = "SELECT t.*, i.material_name, i.material_code, i.unit " +
                     "FROM inventory_transactions t " +
                     "JOIN inventory i ON t.inventory_id = i.inventory_id " +
                     "WHERE t.inventory_id = ? " +
                     "ORDER BY t.transaction_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, inventoryId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching transactions for inventory ID: " + inventoryId, e);
        }
        return list;
    }

    @Override
    public List<InventoryTransaction> findPaginated(int offset, int limit, Integer inventoryId, TransactionType type) {
        List<InventoryTransaction> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT t.*, i.material_name, i.material_code, i.unit " +
                "FROM inventory_transactions t " +
                "JOIN inventory i ON t.inventory_id = i.inventory_id " +
                "WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        if (inventoryId != null && inventoryId > 0) {
            sql.append("AND t.inventory_id = ? ");
            params.add(inventoryId);
        }
        if (type != null) {
            sql.append("AND t.transaction_type = ? ");
            params.add(type.name());
        }

        sql.append("ORDER BY t.transaction_id DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error querying paginated transactions", e);
        }
        return list;
    }

    @Override
    public int countTotal(Integer inventoryId, TransactionType type) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM inventory_transactions WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (inventoryId != null && inventoryId > 0) {
            sql.append("AND inventory_id = ? ");
            params.add(inventoryId);
        }
        if (type != null) {
            sql.append("AND transaction_type = ? ");
            params.add(type.name());
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting inventory transactions", e);
        }
    }

    @Override
    public boolean create(InventoryTransaction transaction) {
        try (Connection conn = DBConnection.getConnection()) {
            return create(transaction, conn);
        } catch (SQLException e) {
            throw new DatabaseException("Error creating inventory transaction", e);
        }
    }

    @Override
    public boolean create(InventoryTransaction transaction, Connection conn) {
        String sql = "INSERT INTO inventory_transactions (inventory_id, transaction_type, quantity, reference_type, reference_id) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, transaction.getInventoryId());
            stmt.setString(2, transaction.getTransactionType().name());
            stmt.setInt(3, transaction.getQuantity());
            stmt.setString(4, transaction.getReferenceType());
            if (transaction.getReferenceId() != null) {
                stmt.setInt(5, transaction.getReferenceId());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        transaction.setTransactionId(keys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new DatabaseException("Error creating inventory transaction in transaction context", e);
        }
    }
}
