package com.packflow.dao.impl;

import com.packflow.dao.UserDAO;
import com.packflow.exception.DatabaseException;
import com.packflow.model.CustomerUser;
import com.packflow.model.Role;
import com.packflow.model.User;
import com.packflow.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAOImpl implements UserDAO {

    private User mapRow(ResultSet rs) throws SQLException {
        int userId = rs.getInt("user_id");
        String name = rs.getString("name");
        String email = rs.getString("email");
        String passwordHash = rs.getString("password_hash");
        Role role = Role.fromString(rs.getString("role"));
        String status = rs.getString("status");

        User user = User.create(
                userId,
                name,
                email,
                passwordHash,
                role,
                status,
                rs.getTimestamp("created_at"),
                rs.getTimestamp("updated_at")
        );

        if (user instanceof CustomerUser cu) {
            try {
                int custId = rs.getInt("customer_id");
                if (!rs.wasNull()) {
                    cu.setCustomerId(custId);
                    cu.setCompanyName(rs.getString("company_name"));
                }
            } catch (SQLException ignored) {
            }
        }
        return user;
    }

    @Override
    public Optional<User> findById(int userId) {
        String sql = "SELECT u.*, c.customer_id, c.company_name " +
                     "FROM users u " +
                     "LEFT JOIN customers c ON u.user_id = c.user_id " +
                     "WHERE u.user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding user by ID: " + userId, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT u.*, c.customer_id, c.company_name " +
                     "FROM users u " +
                     "LEFT JOIN customers c ON u.user_id = c.user_id " +
                     "WHERE LOWER(u.email) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email != null ? email.trim() : "");
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding user by email: " + email, e);
        }
        return Optional.empty();
    }

    @Override
    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT u.*, c.customer_id, c.company_name " +
                     "FROM users u " +
                     "LEFT JOIN customers c ON u.user_id = c.user_id " +
                     "ORDER BY u.user_id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching all users", e);
        }
        return list;
    }

    @Override
    public List<User> findByRole(Role role) {
        List<User> list = new ArrayList<>();
        String sql = "SELECT u.*, c.customer_id, c.company_name " +
                     "FROM users u " +
                     "LEFT JOIN customers c ON u.user_id = c.user_id " +
                     "WHERE u.role = ? " +
                     "ORDER BY u.name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, role.name());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding users by role: " + role, e);
        }
        return list;
    }

    @Override
    public boolean create(User user) {
        String sql = "INSERT INTO users (name, email, password_hash, role, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPasswordHash());
            stmt.setString(4, user.getRole().name());
            stmt.setString(5, user.getStatus());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        user.setUserId(keys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new DatabaseException("Error creating user: " + user.getEmail(), e);
        }
    }

    @Override
    public boolean update(User user) {
        String sql = "UPDATE users SET name = ?, email = ?, role = ?, status = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getRole().name());
            stmt.setString(4, user.getStatus());
            stmt.setInt(5, user.getUserId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating user ID: " + user.getUserId(), e);
        }
    }

    @Override
    public boolean updatePassword(int userId, String newPasswordHash) {
        String sql = "UPDATE users SET password_hash = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newPasswordHash);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating password for user ID: " + userId, e);
        }
    }

    @Override
    public boolean delete(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting user ID: " + userId, e);
        }
    }

    @Override
    public int countTotal() {
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error counting users", e);
        }
    }
}
