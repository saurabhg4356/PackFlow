package com.packflow.dao.impl;

import com.packflow.dao.QualityCheckDAO;
import com.packflow.exception.DatabaseException;
import com.packflow.model.QualityCheck;
import com.packflow.model.QualityStatus;
import com.packflow.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class QualityCheckDAOImpl implements QualityCheckDAO {

    private QualityCheck mapRow(ResultSet rs) throws SQLException {
        QualityCheck qc = new QualityCheck();
        qc.setQualityCheckId(rs.getInt("quality_check_id"));
        qc.setOrderId(rs.getInt("order_id"));
        int checkedBy = rs.getInt("checked_by");
        if (!rs.wasNull()) {
            qc.setCheckedBy(checkedBy);
        } else {
            qc.setCheckedBy(null);
        }
        qc.setQuantityChecked(rs.getInt("quantity_checked"));
        qc.setQuantityPassed(rs.getInt("quantity_passed"));
        qc.setQuantityFailed(rs.getInt("quantity_failed"));
        qc.setRemarks(rs.getString("remarks"));
        qc.setStatus(QualityStatus.fromString(rs.getString("status")));
        qc.setCheckedAt(rs.getTimestamp("checked_at"));

        try {
            qc.setOrderNumber(rs.getString("order_number"));
            qc.setCheckedByName(rs.getString("checked_name"));
            qc.setCompanyName(rs.getString("company_name"));
        } catch (SQLException ignored) {
        }
        return qc;
    }

    @Override
    public Optional<QualityCheck> findById(int qualityCheckId) {
        String sql = "SELECT qc.*, o.order_number, u.name AS checked_name, c.company_name " +
                     "FROM quality_checks qc " +
                     "JOIN orders o ON qc.order_id = o.order_id " +
                     "JOIN customers c ON o.customer_id = c.customer_id " +
                     "LEFT JOIN users u ON qc.checked_by = u.user_id " +
                     "WHERE qc.quality_check_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, qualityCheckId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding quality check by ID: " + qualityCheckId, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<QualityCheck> findByOrderId(int orderId) {
        String sql = "SELECT qc.*, o.order_number, u.name AS checked_name, c.company_name " +
                     "FROM quality_checks qc " +
                     "JOIN orders o ON qc.order_id = o.order_id " +
                     "JOIN customers c ON o.customer_id = c.customer_id " +
                     "LEFT JOIN users u ON qc.checked_by = u.user_id " +
                     "WHERE qc.order_id = ? " +
                     "ORDER BY qc.quality_check_id DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding quality check for order ID: " + orderId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<QualityCheck> findAll() {
        List<QualityCheck> list = new ArrayList<>();
        String sql = "SELECT qc.*, o.order_number, u.name AS checked_name, c.company_name " +
                     "FROM quality_checks qc " +
                     "JOIN orders o ON qc.order_id = o.order_id " +
                     "JOIN customers c ON o.customer_id = c.customer_id " +
                     "LEFT JOIN users u ON qc.checked_by = u.user_id " +
                     "ORDER BY qc.quality_check_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching all quality checks", e);
        }
        return list;
    }

    @Override
    public List<QualityCheck> findPaginated(int offset, int limit, QualityStatus status) {
        List<QualityCheck> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT qc.*, o.order_number, u.name AS checked_name, c.company_name " +
                "FROM quality_checks qc " +
                "JOIN orders o ON qc.order_id = o.order_id " +
                "JOIN customers c ON o.customer_id = c.customer_id " +
                "LEFT JOIN users u ON qc.checked_by = u.user_id " +
                "WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        if (status != null) {
            sql.append("AND qc.status = ? ");
            params.add(status.name());
        }

        sql.append("ORDER BY qc.quality_check_id DESC LIMIT ? OFFSET ?");
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
            throw new DatabaseException("Error querying paginated quality checks", e);
        }
        return list;
    }

    @Override
    public int countTotal(QualityStatus status) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM quality_checks WHERE 1=1 ");
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
            throw new DatabaseException("Error counting quality checks", e);
        }
    }

    @Override
    public boolean create(QualityCheck qc) {
        try (Connection conn = DBConnection.getConnection()) {
            return create(qc, conn);
        } catch (SQLException e) {
            throw new DatabaseException("Error creating quality check", e);
        }
    }

    @Override
    public boolean create(QualityCheck qc, Connection conn) {
        String sql = "INSERT INTO quality_checks (order_id, checked_by, quantity_checked, quantity_passed, quantity_failed, remarks, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, qc.getOrderId());
            if (qc.getCheckedBy() != null) {
                stmt.setInt(2, qc.getCheckedBy());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            stmt.setInt(3, qc.getQuantityChecked());
            stmt.setInt(4, qc.getQuantityPassed());
            stmt.setInt(5, qc.getQuantityFailed());
            stmt.setString(6, qc.getRemarks());
            stmt.setString(7, qc.getStatus().name());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        qc.setQualityCheckId(keys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new DatabaseException("Error creating quality check in transaction for order ID: " + qc.getOrderId(), e);
        }
    }

    @Override
    public boolean update(QualityCheck qc) {
        String sql = "UPDATE quality_checks SET checked_by = ?, quantity_checked = ?, quantity_passed = ?, quantity_failed = ?, remarks = ?, status = ? " +
                     "WHERE quality_check_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (qc.getCheckedBy() != null) {
                stmt.setInt(1, qc.getCheckedBy());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setInt(2, qc.getQuantityChecked());
            stmt.setInt(3, qc.getQuantityPassed());
            stmt.setInt(4, qc.getQuantityFailed());
            stmt.setString(5, qc.getRemarks());
            stmt.setString(6, qc.getStatus().name());
            stmt.setInt(7, qc.getQualityCheckId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating quality check ID: " + qc.getQualityCheckId(), e);
        }
    }

    @Override
    public boolean delete(int qualityCheckId) {
        String sql = "DELETE FROM quality_checks WHERE quality_check_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, qualityCheckId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting quality check ID: " + qualityCheckId, e);
        }
    }
}
