package com.packflow.dao.impl;

import com.packflow.dao.InventoryDAO;
import com.packflow.exception.DatabaseException;
import com.packflow.model.InventoryItem;
import com.packflow.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InventoryDAOImpl implements InventoryDAO {

    private InventoryItem mapRow(ResultSet rs) throws SQLException {
        return new InventoryItem(
                rs.getInt("inventory_id"),
                rs.getString("material_name"),
                rs.getString("material_code"),
                rs.getInt("quantity_available"),
                rs.getInt("quantity_reserved"),
                rs.getInt("reorder_level"),
                rs.getString("unit"),
                rs.getTimestamp("updated_at")
        );
    }

    @Override
    public Optional<InventoryItem> findById(int inventoryId) {
        try (Connection conn = DBConnection.getConnection()) {
            return findById(inventoryId, conn);
        } catch (SQLException e) {
            throw new DatabaseException("Error finding inventory by ID: " + inventoryId, e);
        }
    }

    @Override
    public Optional<InventoryItem> findById(int inventoryId, Connection conn) {
        String sql = "SELECT * FROM inventory WHERE inventory_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, inventoryId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding inventory by ID in transaction: " + inventoryId, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<InventoryItem> findByCode(String materialCode) {
        String sql = "SELECT * FROM inventory WHERE LOWER(material_code) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, materialCode != null ? materialCode.trim() : "");
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding inventory by code: " + materialCode, e);
        }
        return Optional.empty();
    }

    @Override
    public List<InventoryItem> findAll() {
        List<InventoryItem> list = new ArrayList<>();
        String sql = "SELECT * FROM inventory ORDER BY material_name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching all inventory items", e);
        }
        return list;
    }

    @Override
    public List<InventoryItem> findLowStock() {
        List<InventoryItem> list = new ArrayList<>();
        String sql = "SELECT * FROM inventory WHERE quantity_available <= reorder_level ORDER BY quantity_available ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching low stock items", e);
        }
        return list;
    }

    @Override
    public List<InventoryItem> findPaginated(int offset, int limit, String search, String stockFilter) {
        List<InventoryItem> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM inventory WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            sql.append("AND (material_name LIKE ? OR material_code LIKE ?) ");
            String p = "%" + search.trim() + "%";
            params.add(p);
            params.add(p);
        }

        if ("LOW".equalsIgnoreCase(stockFilter)) {
            sql.append("AND quantity_available > 0 AND quantity_available <= reorder_level ");
        } else if ("OUT".equalsIgnoreCase(stockFilter)) {
            sql.append("AND quantity_available <= 0 ");
        } else if ("HEALTHY".equalsIgnoreCase(stockFilter)) {
            sql.append("AND quantity_available > reorder_level ");
        }

        sql.append("ORDER BY inventory_id ASC LIMIT ? OFFSET ?");
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
            throw new DatabaseException("Error querying paginated inventory items", e);
        }
        return list;
    }

    @Override
    public int countTotal(String search, String stockFilter) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM inventory WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            sql.append("AND (material_name LIKE ? OR material_code LIKE ?) ");
            String p = "%" + search.trim() + "%";
            params.add(p);
            params.add(p);
        }

        if ("LOW".equalsIgnoreCase(stockFilter)) {
            sql.append("AND quantity_available > 0 AND quantity_available <= reorder_level ");
        } else if ("OUT".equalsIgnoreCase(stockFilter)) {
            sql.append("AND quantity_available <= 0 ");
        } else if ("HEALTHY".equalsIgnoreCase(stockFilter)) {
            sql.append("AND quantity_available > reorder_level ");
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
            throw new DatabaseException("Error counting inventory records", e);
        }
    }

    @Override
    public boolean create(InventoryItem item) {
        String sql = "INSERT INTO inventory (material_name, material_code, quantity_available, quantity_reserved, reorder_level, unit) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, item.getMaterialName());
            stmt.setString(2, item.getMaterialCode());
            stmt.setInt(3, item.getQuantityAvailable());
            stmt.setInt(4, item.getQuantityReserved());
            stmt.setInt(5, item.getReorderLevel());
            stmt.setString(6, item.getUnit());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        item.setInventoryId(keys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new DatabaseException("Error creating inventory material: " + item.getMaterialCode(), e);
        }
    }

    @Override
    public boolean update(InventoryItem item) {
        String sql = "UPDATE inventory SET material_name = ?, material_code = ?, quantity_available = ?, quantity_reserved = ?, reorder_level = ?, unit = ? " +
                     "WHERE inventory_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, item.getMaterialName());
            stmt.setString(2, item.getMaterialCode());
            stmt.setInt(3, item.getQuantityAvailable());
            stmt.setInt(4, item.getQuantityReserved());
            stmt.setInt(5, item.getReorderLevel());
            stmt.setString(6, item.getUnit());
            stmt.setInt(7, item.getInventoryId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating inventory ID: " + item.getInventoryId(), e);
        }
    }

    @Override
    public boolean reserveStock(int inventoryId, int quantity, Connection conn) {
        String sql = "UPDATE inventory SET quantity_available = quantity_available - ?, quantity_reserved = quantity_reserved + ? " +
                     "WHERE inventory_id = ? AND quantity_available >= ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantity);
            stmt.setInt(2, quantity);
            stmt.setInt(3, inventoryId);
            stmt.setInt(4, quantity);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error reserving stock for inventory ID: " + inventoryId, e);
        }
    }

    @Override
    public boolean releaseStock(int inventoryId, int quantity, Connection conn) {
        String sql = "UPDATE inventory SET quantity_available = quantity_available + ?, quantity_reserved = quantity_reserved - ? " +
                     "WHERE inventory_id = ? AND quantity_reserved >= ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantity);
            stmt.setInt(2, quantity);
            stmt.setInt(3, inventoryId);
            stmt.setInt(4, quantity);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error releasing stock for inventory ID: " + inventoryId, e);
        }
    }

    @Override
    public boolean consumeStock(int inventoryId, int quantity, Connection conn) {
        String sql = "UPDATE inventory SET quantity_reserved = quantity_reserved - ? " +
                     "WHERE inventory_id = ? AND quantity_reserved >= ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantity);
            stmt.setInt(2, inventoryId);
            stmt.setInt(3, quantity);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error consuming stock for inventory ID: " + inventoryId, e);
        }
    }

    @Override
    public boolean adjustStock(int inventoryId, int newAvailable, Connection conn) {
        String sql = "UPDATE inventory SET quantity_available = ? WHERE inventory_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newAvailable);
            stmt.setInt(2, inventoryId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error adjusting stock for inventory ID: " + inventoryId, e);
        }
    }

    @Override
    public boolean delete(int inventoryId) {
        String sql = "DELETE FROM inventory WHERE inventory_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, inventoryId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting inventory item ID: " + inventoryId, e);
        }
    }
}
