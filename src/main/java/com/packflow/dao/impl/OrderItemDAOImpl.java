package com.packflow.dao.impl;

import com.packflow.dao.OrderItemDAO;
import com.packflow.exception.DatabaseException;
import com.packflow.model.OrderItem;
import com.packflow.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class OrderItemDAOImpl implements OrderItemDAO {

    private OrderItem mapRow(ResultSet rs) throws SQLException {
        OrderItem item = new OrderItem();
        item.setOrderItemId(rs.getInt("order_item_id"));
        item.setOrderId(rs.getInt("order_id"));
        item.setProductId(rs.getInt("product_id"));
        item.setServiceId(rs.getInt("service_id"));
        item.setQuantity(rs.getInt("quantity"));
        item.setUnitPrice(rs.getBigDecimal("unit_price"));
        item.setTotalPrice(rs.getBigDecimal("total_price"));

        try {
            item.setProductName(rs.getString("product_name"));
            item.setProductCode(rs.getString("product_code"));
            item.setServiceName(rs.getString("service_name"));
        } catch (SQLException ignored) {
        }
        return item;
    }

    @Override
    public List<OrderItem> findByOrderId(int orderId) {
        try (Connection conn = DBConnection.getConnection()) {
            return findByOrderId(orderId, conn);
        } catch (SQLException e) {
            throw new DatabaseException("Error finding items for order ID: " + orderId, e);
        }
    }

    @Override
    public List<OrderItem> findByOrderId(int orderId, Connection conn) {
        List<OrderItem> list = new ArrayList<>();
        String sql = "SELECT oi.*, p.product_name, p.product_code, ps.service_name " +
                     "FROM order_items oi " +
                     "JOIN products p ON oi.product_id = p.product_id " +
                     "JOIN packaging_services ps ON oi.service_id = ps.service_id " +
                     "WHERE oi.order_id = ? " +
                     "ORDER BY oi.order_item_id ASC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching items for order ID: " + orderId, e);
        }
        return list;
    }

    @Override
    public boolean create(OrderItem item) {
        try (Connection conn = DBConnection.getConnection()) {
            return create(item, conn);
        } catch (SQLException e) {
            throw new DatabaseException("Error creating order item", e);
        }
    }

    @Override
    public boolean create(OrderItem item, Connection conn) {
        String sql = "INSERT INTO order_items (order_id, product_id, service_id, quantity, unit_price, total_price) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            item.calculateTotalPrice();

            stmt.setInt(1, item.getOrderId());
            stmt.setInt(2, item.getProductId());
            stmt.setInt(3, item.getServiceId());
            stmt.setInt(4, item.getQuantity());
            stmt.setBigDecimal(5, item.getUnitPrice());
            stmt.setBigDecimal(6, item.getTotalPrice());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        item.setOrderItemId(keys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new DatabaseException("Error creating order item for order ID: " + item.getOrderId(), e);
        }
    }

    @Override
    public boolean createBatch(List<OrderItem> items, int orderId, Connection conn) {
        if (items == null || items.isEmpty()) {
            return true;
        }
        String sql = "INSERT INTO order_items (order_id, product_id, service_id, quantity, unit_price, total_price) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (OrderItem item : items) {
                item.setOrderId(orderId);
                item.calculateTotalPrice();

                stmt.setInt(1, orderId);
                stmt.setInt(2, item.getProductId());
                stmt.setInt(3, item.getServiceId());
                stmt.setInt(4, item.getQuantity());
                stmt.setBigDecimal(5, item.getUnitPrice());
                stmt.setBigDecimal(6, item.getTotalPrice());
                stmt.addBatch();
            }
            int[] results = stmt.executeBatch();
            return results.length == items.size();
        } catch (SQLException e) {
            throw new DatabaseException("Error creating batch order items for order ID: " + orderId, e);
        }
    }

    @Override
    public boolean deleteByOrderId(int orderId) {
        try (Connection conn = DBConnection.getConnection()) {
            return deleteByOrderId(orderId, conn);
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting items for order ID: " + orderId, e);
        }
    }

    @Override
    public boolean deleteByOrderId(int orderId, Connection conn) {
        String sql = "DELETE FROM order_items WHERE order_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            return stmt.executeUpdate() >= 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting items in transaction for order ID: " + orderId, e);
        }
    }
}
