package com.packflow.dao.impl;

import com.packflow.dao.OrderDAO;
import com.packflow.exception.DatabaseException;
import com.packflow.model.Order;
import com.packflow.model.OrderStatus;
import com.packflow.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderDAOImpl implements OrderDAO {

    private Order mapRow(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setOrderId(rs.getInt("order_id"));
        order.setOrderNumber(rs.getString("order_number"));
        order.setCustomerId(rs.getInt("customer_id"));
        order.setOrderDate(rs.getDate("order_date"));
        order.setStatus(OrderStatus.fromString(rs.getString("status")));
        order.setSubtotal(rs.getBigDecimal("subtotal"));
        order.setTotalCost(rs.getBigDecimal("total_cost"));
        order.setNotes(rs.getString("notes"));
        order.setCreatedAt(rs.getTimestamp("created_at"));
        order.setUpdatedAt(rs.getTimestamp("updated_at"));

        try {
            order.setCompanyName(rs.getString("company_name"));
            order.setContactPerson(rs.getString("contact_person"));
            order.setCustomerEmail(rs.getString("email"));
            order.setCustomerPhone(rs.getString("phone"));
        } catch (SQLException ignored) {
        }
        return order;
    }

    @Override
    public Optional<Order> findById(int orderId) {
        String sql = "SELECT o.*, c.company_name, c.contact_person, c.email, c.phone " +
                     "FROM orders o " +
                     "JOIN customers c ON o.customer_id = c.customer_id " +
                     "WHERE o.order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding order by ID: " + orderId, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Order> findByOrderNumber(String orderNumber) {
        String sql = "SELECT o.*, c.company_name, c.contact_person, c.email, c.phone " +
                     "FROM orders o " +
                     "JOIN customers c ON o.customer_id = c.customer_id " +
                     "WHERE o.order_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, orderNumber != null ? orderNumber.trim() : "");
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding order by number: " + orderNumber, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Order> findAll() {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT o.*, c.company_name, c.contact_person, c.email, c.phone " +
                     "FROM orders o " +
                     "JOIN customers c ON o.customer_id = c.customer_id " +
                     "ORDER BY o.order_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching all orders", e);
        }
        return list;
    }

    @Override
    public List<Order> findByCustomerId(int customerId) {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT o.*, c.company_name, c.contact_person, c.email, c.phone " +
                     "FROM orders o " +
                     "JOIN customers c ON o.customer_id = c.customer_id " +
                     "WHERE o.customer_id = ? " +
                     "ORDER BY o.order_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching orders for customer ID: " + customerId, e);
        }
        return list;
    }

    @Override
    public List<Order> findPaginated(int offset, int limit, String search, Integer customerId, OrderStatus status, Date fromDate, Date toDate) {
        List<Order> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT o.*, c.company_name, c.contact_person, c.email, c.phone " +
                "FROM orders o " +
                "JOIN customers c ON o.customer_id = c.customer_id " +
                "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            sql.append("AND (o.order_number LIKE ? OR c.company_name LIKE ? OR c.contact_person LIKE ?) ");
            String p = "%" + search.trim() + "%";
            params.add(p);
            params.add(p);
            params.add(p);
        }
        if (customerId != null && customerId > 0) {
            sql.append("AND o.customer_id = ? ");
            params.add(customerId);
        }
        if (status != null) {
            sql.append("AND o.status = ? ");
            params.add(status.name());
        }
        if (fromDate != null) {
            sql.append("AND o.order_date >= ? ");
            params.add(fromDate);
        }
        if (toDate != null) {
            sql.append("AND o.order_date <= ? ");
            params.add(toDate);
        }

        sql.append("ORDER BY o.order_id DESC LIMIT ? OFFSET ?");
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
            throw new DatabaseException("Error querying paginated orders", e);
        }
        return list;
    }

    @Override
    public int countTotal(String search, Integer customerId, OrderStatus status, Date fromDate, Date toDate) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM orders o JOIN customers c ON o.customer_id = c.customer_id WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            sql.append("AND (o.order_number LIKE ? OR c.company_name LIKE ? OR c.contact_person LIKE ?) ");
            String p = "%" + search.trim() + "%";
            params.add(p);
            params.add(p);
            params.add(p);
        }
        if (customerId != null && customerId > 0) {
            sql.append("AND o.customer_id = ? ");
            params.add(customerId);
        }
        if (status != null) {
            sql.append("AND o.status = ? ");
            params.add(status.name());
        }
        if (fromDate != null) {
            sql.append("AND o.order_date >= ? ");
            params.add(fromDate);
        }
        if (toDate != null) {
            sql.append("AND o.order_date <= ? ");
            params.add(toDate);
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
            throw new DatabaseException("Error counting filtered orders", e);
        }
    }

    @Override
    public int create(Order order) {
        try (Connection conn = DBConnection.getConnection()) {
            return create(order, conn);
        } catch (SQLException e) {
            throw new DatabaseException("Error creating order", e);
        }
    }

    @Override
    public int create(Order order, Connection conn) {
        String sql = "INSERT INTO orders (order_number, customer_id, order_date, status, subtotal, total_cost, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (order.getOrderNumber() == null || order.getOrderNumber().isEmpty()) {
                order.setOrderNumber(generateNextOrderNumber(conn));
            }
            if (order.getOrderDate() == null) {
                order.setOrderDate(Date.valueOf(LocalDate.now()));
            }

            stmt.setString(1, order.getOrderNumber());
            stmt.setInt(2, order.getCustomerId());
            stmt.setDate(3, order.getOrderDate());
            stmt.setString(4, order.getStatus().name());
            stmt.setBigDecimal(5, order.getSubtotal());
            stmt.setBigDecimal(6, order.getTotalCost());
            stmt.setString(7, order.getNotes());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        int genId = keys.getInt(1);
                        order.setOrderId(genId);
                        return genId;
                    }
                }
            }
            return 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error creating order with number: " + order.getOrderNumber(), e);
        }
    }

    @Override
    public boolean update(Order order) {
        String sql = "UPDATE orders SET customer_id = ?, order_date = ?, status = ?, subtotal = ?, total_cost = ?, notes = ? " +
                     "WHERE order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, order.getCustomerId());
            stmt.setDate(2, order.getOrderDate());
            stmt.setString(3, order.getStatus().name());
            stmt.setBigDecimal(4, order.getSubtotal());
            stmt.setBigDecimal(5, order.getTotalCost());
            stmt.setString(6, order.getNotes());
            stmt.setInt(7, order.getOrderId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating order ID: " + order.getOrderId(), e);
        }
    }

    @Override
    public boolean updateStatus(int orderId, OrderStatus status) {
        try (Connection conn = DBConnection.getConnection()) {
            return updateStatus(orderId, status, conn);
        } catch (SQLException e) {
            throw new DatabaseException("Error updating status for order ID: " + orderId, e);
        }
    }

    @Override
    public boolean updateStatus(int orderId, OrderStatus status, Connection conn) {
        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setInt(2, orderId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating status in transaction for order ID: " + orderId, e);
        }
    }

    @Override
    public String generateNextOrderNumber() {
        try (Connection conn = DBConnection.getConnection()) {
            return generateNextOrderNumber(conn);
        } catch (SQLException e) {
            throw new DatabaseException("Error generating next order number", e);
        }
    }

    @Override
    public String generateNextOrderNumber(Connection conn) {
        int currentYear = LocalDate.now().getYear();
        String prefix = "PKG-" + currentYear + "-";
        String sql = "SELECT COUNT(*) FROM orders";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            int count = 0;
            if (rs.next()) {
                count = rs.getInt(1);
            }
            int nextSeq = count + 1;
            return String.format("%s%05d", prefix, nextSeq);
        } catch (SQLException e) {
            return prefix + System.currentTimeMillis();
        }
    }

    @Override
    public boolean delete(int orderId) {
        String sql = "DELETE FROM orders WHERE order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting order ID: " + orderId, e);
        }
    }
}
