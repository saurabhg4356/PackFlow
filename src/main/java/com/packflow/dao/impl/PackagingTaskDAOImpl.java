package com.packflow.dao.impl;

import com.packflow.dao.PackagingTaskDAO;
import com.packflow.exception.DatabaseException;
import com.packflow.model.PackagingTask;
import com.packflow.model.TaskStatus;
import com.packflow.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PackagingTaskDAOImpl implements PackagingTaskDAO {

    private PackagingTask mapRow(ResultSet rs) throws SQLException {
        PackagingTask t = new PackagingTask();
        t.setTaskId(rs.getInt("task_id"));
        t.setOrderId(rs.getInt("order_id"));
        int assignedTo = rs.getInt("assigned_to");
        if (!rs.wasNull()) {
            t.setAssignedTo(assignedTo);
        } else {
            t.setAssignedTo(null);
        }
        t.setQuantity(rs.getInt("quantity"));
        t.setCompletedQuantity(rs.getInt("completed_quantity"));
        t.setStatus(TaskStatus.fromString(rs.getString("status")));
        t.setStartedAt(rs.getTimestamp("started_at"));
        t.setCompletedAt(rs.getTimestamp("completed_at"));

        try {
            t.setOrderNumber(rs.getString("order_number"));
            t.setAssignedToName(rs.getString("assigned_name"));
            t.setCompanyName(rs.getString("company_name"));
        } catch (SQLException ignored) {
        }
        return t;
    }

    @Override
    public Optional<PackagingTask> findById(int taskId) {
        String sql = "SELECT pt.*, o.order_number, u.name AS assigned_name, c.company_name " +
                     "FROM packaging_tasks pt " +
                     "JOIN orders o ON pt.order_id = o.order_id " +
                     "JOIN customers c ON o.customer_id = c.customer_id " +
                     "LEFT JOIN users u ON pt.assigned_to = u.user_id " +
                     "WHERE pt.task_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, taskId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding packaging task by ID: " + taskId, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<PackagingTask> findByOrderId(int orderId) {
        String sql = "SELECT pt.*, o.order_number, u.name AS assigned_name, c.company_name " +
                     "FROM packaging_tasks pt " +
                     "JOIN orders o ON pt.order_id = o.order_id " +
                     "JOIN customers c ON o.customer_id = c.customer_id " +
                     "LEFT JOIN users u ON pt.assigned_to = u.user_id " +
                     "WHERE pt.order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding packaging task for order ID: " + orderId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<PackagingTask> findAll() {
        List<PackagingTask> list = new ArrayList<>();
        String sql = "SELECT pt.*, o.order_number, u.name AS assigned_name, c.company_name " +
                     "FROM packaging_tasks pt " +
                     "JOIN orders o ON pt.order_id = o.order_id " +
                     "JOIN customers c ON o.customer_id = c.customer_id " +
                     "LEFT JOIN users u ON pt.assigned_to = u.user_id " +
                     "ORDER BY pt.task_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching all packaging tasks", e);
        }
        return list;
    }

    @Override
    public List<PackagingTask> findPaginated(int offset, int limit, TaskStatus status, Integer assignedTo) {
        List<PackagingTask> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT pt.*, o.order_number, u.name AS assigned_name, c.company_name " +
                "FROM packaging_tasks pt " +
                "JOIN orders o ON pt.order_id = o.order_id " +
                "JOIN customers c ON o.customer_id = c.customer_id " +
                "LEFT JOIN users u ON pt.assigned_to = u.user_id " +
                "WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        if (status != null) {
            sql.append("AND pt.status = ? ");
            params.add(status.name());
        }
        if (assignedTo != null && assignedTo > 0) {
            sql.append("AND pt.assigned_to = ? ");
            params.add(assignedTo);
        }

        sql.append("ORDER BY pt.task_id DESC LIMIT ? OFFSET ?");
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
            throw new DatabaseException("Error querying paginated packaging tasks", e);
        }
        return list;
    }

    @Override
    public int countTotal(TaskStatus status, Integer assignedTo) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM packaging_tasks pt WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (status != null) {
            sql.append("AND pt.status = ? ");
            params.add(status.name());
        }
        if (assignedTo != null && assignedTo > 0) {
            sql.append("AND pt.assigned_to = ? ");
            params.add(assignedTo);
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
            throw new DatabaseException("Error counting packaging tasks", e);
        }
    }

    @Override
    public boolean create(PackagingTask task) {
        try (Connection conn = DBConnection.getConnection()) {
            return create(task, conn);
        } catch (SQLException e) {
            throw new DatabaseException("Error creating packaging task", e);
        }
    }

    @Override
    public boolean create(PackagingTask task, Connection conn) {
        String sql = "INSERT INTO packaging_tasks (order_id, assigned_to, quantity, completed_quantity, status, started_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, task.getOrderId());
            if (task.getAssignedTo() != null) {
                stmt.setInt(2, task.getAssignedTo());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            stmt.setInt(3, task.getQuantity());
            stmt.setInt(4, task.getCompletedQuantity());
            stmt.setString(5, task.getStatus().name());
            if (task.getStatus() == TaskStatus.IN_PROGRESS) {
                stmt.setTimestamp(6, Timestamp.from(Instant.now()));
            } else {
                stmt.setTimestamp(6, task.getStartedAt());
            }

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        task.setTaskId(keys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new DatabaseException("Error creating packaging task in transaction for order ID: " + task.getOrderId(), e);
        }
    }

    @Override
    public boolean update(PackagingTask task) {
        String sql = "UPDATE packaging_tasks SET assigned_to = ?, quantity = ?, completed_quantity = ?, status = ?, started_at = ?, completed_at = ? " +
                     "WHERE task_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (task.getAssignedTo() != null) {
                stmt.setInt(1, task.getAssignedTo());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setInt(2, task.getQuantity());
            stmt.setInt(3, task.getCompletedQuantity());
            stmt.setString(4, task.getStatus().name());
            stmt.setTimestamp(5, task.getStartedAt());
            stmt.setTimestamp(6, task.getCompletedAt());
            stmt.setInt(7, task.getTaskId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating packaging task ID: " + task.getTaskId(), e);
        }
    }

    @Override
    public boolean updateProgress(int taskId, int completedQuantity, TaskStatus status) {
        String sql = "UPDATE packaging_tasks SET completed_quantity = ?, status = ?, " +
                     "completed_at = CASE WHEN ? = 'COMPLETED' THEN CURRENT_TIMESTAMP ELSE completed_at END " +
                     "WHERE task_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, completedQuantity);
            stmt.setString(2, status.name());
            stmt.setString(3, status.name());
            stmt.setInt(4, taskId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating progress for packaging task ID: " + taskId, e);
        }
    }

    @Override
    public boolean delete(int taskId) {
        String sql = "DELETE FROM packaging_tasks WHERE task_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, taskId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting task ID: " + taskId, e);
        }
    }
}
