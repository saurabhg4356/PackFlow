package com.packflow.dao.impl;

import com.packflow.dao.ProductDAO;
import com.packflow.exception.DatabaseException;
import com.packflow.model.Product;
import com.packflow.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductDAOImpl implements ProductDAO {

    private Product mapRow(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setProductId(rs.getInt("product_id"));
        p.setCustomerId(rs.getInt("customer_id"));
        p.setProductName(rs.getString("product_name"));
        p.setProductCode(rs.getString("product_code"));
        p.setDescription(rs.getString("description"));
        p.setUnit(rs.getString("unit"));
        p.setStatus(rs.getString("status"));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        try {
            p.setCompanyName(rs.getString("company_name"));
        } catch (SQLException ignored) {
        }
        return p;
    }

    @Override
    public Optional<Product> findById(int productId) {
        String sql = "SELECT p.*, c.company_name FROM products p " +
                     "JOIN customers c ON p.customer_id = c.customer_id " +
                     "WHERE p.product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding product by ID: " + productId, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Product> findByCode(String productCode) {
        String sql = "SELECT p.*, c.company_name FROM products p " +
                     "JOIN customers c ON p.customer_id = c.customer_id " +
                     "WHERE LOWER(p.product_code) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, productCode != null ? productCode.trim() : "");
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding product by code: " + productCode, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Product> findAll() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT p.*, c.company_name FROM products p " +
                     "JOIN customers c ON p.customer_id = c.customer_id " +
                     "ORDER BY p.product_name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching all products", e);
        }
        return list;
    }

    @Override
    public List<Product> findByCustomerId(int customerId) {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT p.*, c.company_name FROM products p " +
                     "JOIN customers c ON p.customer_id = c.customer_id " +
                     "WHERE p.customer_id = ? " +
                     "ORDER BY p.product_name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding products for customer ID: " + customerId, e);
        }
        return list;
    }

    @Override
    public List<Product> findPaginated(int offset, int limit, String search, Integer customerId) {
        List<Product> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT p.*, c.company_name FROM products p JOIN customers c ON p.customer_id = c.customer_id WHERE 1=1 ");
        boolean hasSearch = search != null && !search.trim().isEmpty();
        if (hasSearch) {
            sql.append("AND (p.product_name LIKE ? OR p.product_code LIKE ? OR c.company_name LIKE ?) ");
        }
        if (customerId != null && customerId > 0) {
            sql.append("AND p.customer_id = ? ");
        }
        sql.append("ORDER BY p.product_id DESC LIMIT ? OFFSET ?");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int pIndex = 1;
            if (hasSearch) {
                String pattern = "%" + search.trim() + "%";
                stmt.setString(pIndex++, pattern);
                stmt.setString(pIndex++, pattern);
                stmt.setString(pIndex++, pattern);
            }
            if (customerId != null && customerId > 0) {
                stmt.setInt(pIndex++, customerId);
            }
            stmt.setInt(pIndex++, limit);
            stmt.setInt(pIndex, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error querying paginated products", e);
        }
        return list;
    }

    @Override
    public int countTotal(String search, Integer customerId) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM products p JOIN customers c ON p.customer_id = c.customer_id WHERE 1=1 ");
        boolean hasSearch = search != null && !search.trim().isEmpty();
        if (hasSearch) {
            sql.append("AND (p.product_name LIKE ? OR p.product_code LIKE ? OR c.company_name LIKE ?) ");
        }
        if (customerId != null && customerId > 0) {
            sql.append("AND p.customer_id = ? ");
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int pIndex = 1;
            if (hasSearch) {
                String pattern = "%" + search.trim() + "%";
                stmt.setString(pIndex++, pattern);
                stmt.setString(pIndex++, pattern);
                stmt.setString(pIndex++, pattern);
            }
            if (customerId != null && customerId > 0) {
                stmt.setInt(pIndex++, customerId);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting products", e);
        }
    }

    @Override
    public boolean create(Product product) {
        String sql = "INSERT INTO products (customer_id, product_name, product_code, description, unit, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, product.getCustomerId());
            stmt.setString(2, product.getProductName());
            stmt.setString(3, product.getProductCode());
            stmt.setString(4, product.getDescription());
            stmt.setString(5, product.getUnit());
            stmt.setString(6, product.getStatus());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        product.setProductId(keys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new DatabaseException("Error creating product: " + product.getProductCode(), e);
        }
    }

    @Override
    public boolean update(Product product) {
        String sql = "UPDATE products SET customer_id = ?, product_name = ?, product_code = ?, description = ?, unit = ?, status = ? WHERE product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, product.getCustomerId());
            stmt.setString(2, product.getProductName());
            stmt.setString(3, product.getProductCode());
            stmt.setString(4, product.getDescription());
            stmt.setString(5, product.getUnit());
            stmt.setString(6, product.getStatus());
            stmt.setInt(7, product.getProductId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating product ID: " + product.getProductId(), e);
        }
    }

    @Override
    public boolean delete(int productId) {
        String sql = "DELETE FROM products WHERE product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting product ID: " + productId, e);
        }
    }
}
