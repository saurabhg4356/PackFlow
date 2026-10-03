package com.packflow.dao.impl;

import com.packflow.dao.DispatchDAO;
import com.packflow.exception.DatabaseException;
import com.packflow.model.DispatchRecord;
import com.packflow.model.DispatchStatus;
import com.packflow.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DispatchDAOImpl implements DispatchDAO {

    private DispatchRecord mapRow(ResultSet rs) throws SQLException {
        DispatchRecord d = new DispatchRecord();
        d.setDispatchId(rs.getInt("dispatch_id"));
        d.setOrderId(rs.getInt("order_id"));
        d.setDispatchDate(rs.getDate("dispatch_date"));
        d.setDeliveryPartner(rs.getString("delivery_partner"));
        d.setTrackingNumber(rs.getString("tracking_number"));
        d.setStatus(DispatchStatus.fromString(rs.getString("status")));
        d.setDeliveredAt(rs.getTimestamp("delivered_at"));

        try {
            d.setOrderNumber(rs.getString("order_number"));
            d.setCompanyName(rs.getString("company_name"));
            d.setContactPerson(rs.getString("contact_person"));
        } catch (SQLException ignored) {
        }
        return d;
    }

    @Override
    public Optional<DispatchRecord> findById(int dispatchId) {
        String sql = "SELECT d.*, o.order_number, c.company_name, c.contact_person " +
                     "FROM dispatches d " +
                     "JOIN orders o ON d.order_id = o.order_id " +
                     "JOIN customers c ON o.customer_id = c.customer_id " +
                     "WHERE d.dispatch_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, dispatchId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding dispatch by ID: " + dispatchId, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<DispatchRecord> findByOrderId(int orderId) {
        String sql = "SELECT d.*, o.order_number, c.company_name, c.contact_person " +
                     "FROM dispatches d " +
                     "JOIN orders o ON d.order_id = o.order_id " +
                     "JOIN customers c ON o.customer_id = c.customer_id " +
                     "WHERE d.order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding dispatch for order ID: " + orderId, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<DispatchRecord> findByTrackingNumber(String trackingNumber) {
        String sql = "SELECT d.*, o.order_number, c.company_name, c.contact_person " +
                     "FROM dispatches d " +
                     "JOIN orders o ON d.order_id = o.order_id " +
                     "JOIN customers c ON o.customer_id = c.customer_id " +
                     "WHERE LOWER(d.tracking_number) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, trackingNumber != null ? trackingNumber.trim() : "");
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding dispatch by tracking number: " + trackingNumber, e);
        }
        return Optional.empty();
    }

    @Override
    public List<DispatchRecord> findAll() {
        List<DispatchRecord> list = new ArrayList<>();
        String sql = "SELECT d.*, o.order_number, c.company_name, c.contact_person " +
                     "FROM dispatches d " +
                     "JOIN orders o ON d.order_id = o.order_id " +
                     "JOIN customers c ON o.customer_id = c.customer_id " +
                     "ORDER BY d.dispatch_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching all dispatches", e);
        }
        return list;
    }

    @Override
    public List<DispatchRecord> findPaginated(int offset, int limit, DispatchStatus status) {
        List<DispatchRecord> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT d.*, o.order_number, c.company_name, c.contact_person " +
                "FROM dispatches d " +
                "JOIN orders o ON d.order_id = o.order_id " +
                "JOIN customers c ON o.customer_id = c.customer_id " +
                "WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        if (status != null) {
            sql.append("AND d.status = ? ");
            params.add(status.name());
        }

        sql.append("ORDER BY d.dispatch_id DESC LIMIT ? OFFSET ?");
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
            throw new DatabaseException("Error querying paginated dispatches", e);
        }
        return list;
    }

    @Override
    public int countTotal(DispatchStatus status) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM dispatches WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (status != null) {
            sql.append("AND status = ? ");
            params.add(status.name());
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
            throw new DatabaseException("Error counting dispatches", e);
        }
    }

    @Override
    public boolean create(DispatchRecord dispatch) {
        try (Connection conn = DBConnection.getConnection()) {
            return create(dispatch, conn);
        } catch (SQLException e) {
            throw new DatabaseException("Error creating dispatch record", e);
        }
    }

    @Override
    public boolean create(DispatchRecord dispatch, Connection conn) {
        String sql = "INSERT INTO dispatches (order_id, dispatch_date, delivery_partner, tracking_number, status) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, dispatch.getOrderId());
            stmt.setDate(2, dispatch.getDispatchDate());
            stmt.setString(3, dispatch.getDeliveryPartner());
            stmt.setString(4, dispatch.getTrackingNumber());
            stmt.setString(5, dispatch.getStatus().name());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        dispatch.setDispatchId(keys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new DatabaseException("Error creating dispatch in transaction for order ID: " + dispatch.getOrderId(), e);
        }
    }

    @Override
    public boolean update(DispatchRecord dispatch) {
        String sql = "UPDATE dispatches SET dispatch_date = ?, delivery_partner = ?, tracking_number = ?, status = ?, delivered_at = ? " +
                     "WHERE dispatch_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, dispatch.getDispatchDate());
            stmt.setString(2, dispatch.getDeliveryPartner());
            stmt.setString(3, dispatch.getTrackingNumber());
            stmt.setString(4, dispatch.getStatus().name());
            stmt.setTimestamp(5, dispatch.getDeliveredAt());
            stmt.setInt(6, dispatch.getDispatchId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating dispatch ID: " + dispatch.getDispatchId(), e);
        }
    }

    @Override
    public boolean updateStatus(int dispatchId, DispatchStatus status) {
        String sql = "UPDATE dispatches SET status = ?, " +
                     "delivered_at = CASE WHEN ? = 'DELIVERED' THEN CURRENT_TIMESTAMP ELSE delivered_at END " +
                     "WHERE dispatch_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setString(2, status.name());
            stmt.setInt(3, dispatchId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating dispatch status for ID: " + dispatchId, e);
        }
    }

    @Override
    public boolean delete(int dispatchId) {
        String sql = "DELETE FROM dispatches WHERE dispatch_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, dispatchId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting dispatch ID: " + dispatchId, e);
        }
    }
}
