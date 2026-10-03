package com.packflow.dao.impl;

import com.packflow.dao.CustomerDAO;
import com.packflow.exception.DatabaseException;
import com.packflow.model.Customer;
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

public class CustomerDAOImpl implements CustomerDAO {

    private Customer mapRow(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setCustomerId(rs.getInt("customer_id"));
        int userId = rs.getInt("user_id");
        if (!rs.wasNull()) {
            c.setUserId(userId);
        } else {
            c.setUserId(null);
        }
        c.setCompanyName(rs.getString("company_name"));
        c.setContactPerson(rs.getString("contact_person"));
        c.setPhone(rs.getString("phone"));
        c.setEmail(rs.getString("email"));
        c.setAddress(rs.getString("address"));
        c.setCreatedAt(rs.getTimestamp("created_at"));
        return c;
    }

    @Override
    public Optional<Customer> findById(int customerId) {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding customer by ID: " + customerId, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Customer> findByUserId(int userId) {
        String sql = "SELECT * FROM customers WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding customer by user ID: " + userId, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Customer> findByEmail(String email) {
        String sql = "SELECT * FROM customers WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email != null ? email.trim() : "");
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding customer by email: " + email, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Customer> findAll() {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM customers ORDER BY company_name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching all customers", e);
        }
        return list;
    }

    @Override
    public List<Customer> findPaginated(int offset, int limit, String search) {
        List<Customer> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM customers ");
        boolean hasSearch = search != null && !search.trim().isEmpty();
        if (hasSearch) {
            sql.append("WHERE company_name LIKE ? OR contact_person LIKE ? OR email LIKE ? OR phone LIKE ? ");
        }
        sql.append("ORDER BY customer_id DESC LIMIT ? OFFSET ?");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int pIndex = 1;
            if (hasSearch) {
                String pattern = "%" + search.trim() + "%";
                stmt.setString(pIndex++, pattern);
                stmt.setString(pIndex++, pattern);
                stmt.setString(pIndex++, pattern);
                stmt.setString(pIndex++, pattern);
            }
            stmt.setInt(pIndex++, limit);
            stmt.setInt(pIndex, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error querying paginated customers", e);
        }
        return list;
    }

    @Override
    public int countTotal(String search) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM customers ");
        boolean hasSearch = search != null && !search.trim().isEmpty();
        if (hasSearch) {
            sql.append("WHERE company_name LIKE ? OR contact_person LIKE ? OR email LIKE ? OR phone LIKE ?");
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            if (hasSearch) {
                String pattern = "%" + search.trim() + "%";
                stmt.setString(1, pattern);
                stmt.setString(2, pattern);
                stmt.setString(3, pattern);
                stmt.setString(4, pattern);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting customers", e);
        }
    }

    @Override
    public boolean create(Customer customer) {
        String sql = "INSERT INTO customers (user_id, company_name, contact_person, phone, email, address) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (customer.getUserId() != null) {
                stmt.setInt(1, customer.getUserId());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setString(2, customer.getCompanyName());
            stmt.setString(3, customer.getContactPerson());
            stmt.setString(4, customer.getPhone());
            stmt.setString(5, customer.getEmail());
            stmt.setString(6, customer.getAddress());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        customer.setCustomerId(keys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new DatabaseException("Error creating customer: " + customer.getCompanyName(), e);
        }
    }

    @Override
    public boolean update(Customer customer) {
        String sql = "UPDATE customers SET user_id = ?, company_name = ?, contact_person = ?, phone = ?, email = ?, address = ? WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (customer.getUserId() != null) {
                stmt.setInt(1, customer.getUserId());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setString(2, customer.getCompanyName());
            stmt.setString(3, customer.getContactPerson());
            stmt.setString(4, customer.getPhone());
            stmt.setString(5, customer.getEmail());
            stmt.setString(6, customer.getAddress());
            stmt.setInt(7, customer.getCustomerId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating customer ID: " + customer.getCustomerId(), e);
        }
    }

    @Override
    public boolean delete(int customerId) {
        String sql = "DELETE FROM customers WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting customer ID: " + customerId, e);
        }
    }
}
