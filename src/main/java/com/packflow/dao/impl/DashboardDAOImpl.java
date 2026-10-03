package com.packflow.dao.impl;

import com.packflow.dao.DashboardDAO;
import com.packflow.exception.DatabaseException;
import com.packflow.model.ChartPoint;
import com.packflow.model.DashboardStats;
import com.packflow.model.InventoryItem;
import com.packflow.model.Order;
import com.packflow.model.OrderStatus;
import com.packflow.util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Live Database-Backed Dashboard Analytics implementation.
 * ALL NUMBERS, METRICS, BREAKDOWNS, AND CHARTS ARE DIRECTLY
 * CALCULATED VIA REAL MYSQL QUERIES.
 */
public class DashboardDAOImpl implements DashboardDAO {

    private void applyDateFilter(StringBuilder sql, List<Object> params, String dateFilter, Date startDate, Date endDate) {
        if (dateFilter == null || "all".equalsIgnoreCase(dateFilter)) {
            return;
        }

        LocalDate today = LocalDate.now();
        switch (dateFilter.toLowerCase()) {
            case "today" -> {
                sql.append(" AND order_date = ? ");
                params.add(Date.valueOf(today));
            }
            case "this_week" -> {
                LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
                sql.append(" AND order_date >= ? AND order_date <= ? ");
                params.add(Date.valueOf(startOfWeek));
                params.add(Date.valueOf(today));
            }
            case "this_month" -> {
                LocalDate startOfMonth = today.withDayOfMonth(1);
                sql.append(" AND order_date >= ? AND order_date <= ? ");
                params.add(Date.valueOf(startOfMonth));
                params.add(Date.valueOf(today));
            }
            case "this_year" -> {
                LocalDate startOfYear = today.withDayOfYear(1);
                sql.append(" AND order_date >= ? AND order_date <= ? ");
                params.add(Date.valueOf(startOfYear));
                params.add(Date.valueOf(today));
            }
            case "custom" -> {
                if (startDate != null) {
                    sql.append(" AND order_date >= ? ");
                    params.add(startDate);
                }
                if (endDate != null) {
                    sql.append(" AND order_date <= ? ");
                    params.add(endDate);
                }
            }
        }
    }

    @Override
    public int getTotalOrders(String dateFilter, Date startDate, Date endDate) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM orders WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        applyDateFilter(sql, params, dateFilter, startDate, endDate);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error querying total orders count", e);
        }
        return 0;
    }

    @Override
    public int getPendingOrders() {
        String sql = "SELECT COUNT(*) FROM orders WHERE status = 'PENDING'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error querying pending orders count", e);
        }
        return 0;
    }

    @Override
    public int getProcessingOrders() {
        String sql = "SELECT COUNT(*) FROM orders WHERE status IN ('APPROVED', 'PROCESSING', 'QUALITY_CHECK')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error querying processing orders count", e);
        }
        return 0;
    }

    @Override
    public int getCompletedOrders() {
        String sql = "SELECT COUNT(*) FROM orders WHERE status IN ('COMPLETED', 'DISPATCHED', 'DELIVERED')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error querying completed orders count", e);
        }
        return 0;
    }

    @Override
    public int getTotalCustomers() {
        String sql = "SELECT COUNT(*) FROM customers";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error querying total customers count", e);
        }
        return 0;
    }

    @Override
    public int getTotalProducts() {
        String sql = "SELECT COUNT(*) FROM products";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error querying total products count", e);
        }
        return 0;
    }

    @Override
    public int getLowStockCount() {
        String sql = "SELECT COUNT(*) FROM inventory WHERE quantity_available <= reorder_level";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error querying low stock inventory count", e);
        }
        return 0;
    }

    @Override
    public BigDecimal getTotalRevenue(String dateFilter, Date startDate, Date endDate) {
        StringBuilder sql = new StringBuilder("SELECT COALESCE(SUM(total_cost), 0) FROM orders WHERE status != 'CANCELLED' ");
        List<Object> params = new ArrayList<>();
        applyDateFilter(sql, params, dateFilter, startDate, endDate);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error querying total revenue", e);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public Map<String, Integer> getOrdersByStatus(String dateFilter, Date startDate, Date endDate) {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            map.put(status.name(), 0);
        }

        StringBuilder sql = new StringBuilder("SELECT status, COUNT(*) AS cnt FROM orders WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        applyDateFilter(sql, params, dateFilter, startDate, endDate);
        sql.append(" GROUP BY status");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("status"), rs.getInt("cnt"));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error querying orders by status breakdown", e);
        }
        return map;
    }

    @Override
    public List<ChartPoint> getMonthlyOrders() {
        List<ChartPoint> list = new ArrayList<>();
        String sql = "SELECT DATE_FORMAT(order_date, '%b %Y') AS month_label, " +
                     "YEAR(order_date) AS yr, MONTH(order_date) AS mn, COUNT(*) AS cnt " +
                     "FROM orders " +
                     "GROUP BY YEAR(order_date), MONTH(order_date), DATE_FORMAT(order_date, '%b %Y') " +
                     "ORDER BY yr ASC, mn ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new ChartPoint(rs.getString("month_label"), rs.getDouble("cnt")));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error querying monthly orders chart data", e);
        }
        return list;
    }

    @Override
    public List<ChartPoint> getMonthlyRevenue() {
        List<ChartPoint> list = new ArrayList<>();
        String sql = "SELECT DATE_FORMAT(order_date, '%b %Y') AS month_label, " +
                     "YEAR(order_date) AS yr, MONTH(order_date) AS mn, COALESCE(SUM(total_cost), 0) AS rev " +
                     "FROM orders " +
                     "WHERE status != 'CANCELLED' " +
                     "GROUP BY YEAR(order_date), MONTH(order_date), DATE_FORMAT(order_date, '%b %Y') " +
                     "ORDER BY yr ASC, mn ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new ChartPoint(rs.getString("month_label"), rs.getDouble("rev")));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error querying monthly revenue chart data", e);
        }
        return list;
    }

    @Override
    public Map<String, Integer> getInventoryStatus() {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("HEALTHY", 0);
        map.put("LOW_STOCK", 0);
        map.put("OUT_OF_STOCK", 0);

        String sql = "SELECT " +
                     "SUM(CASE WHEN quantity_available > reorder_level THEN 1 ELSE 0 END) AS healthy_count, " +
                     "SUM(CASE WHEN quantity_available > 0 AND quantity_available <= reorder_level THEN 1 ELSE 0 END) AS low_count, " +
                     "SUM(CASE WHEN quantity_available <= 0 THEN 1 ELSE 0 END) AS out_count " +
                     "FROM inventory";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                map.put("HEALTHY", rs.getInt("healthy_count"));
                map.put("LOW_STOCK", rs.getInt("low_count"));
                map.put("OUT_OF_STOCK", rs.getInt("out_count"));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error querying inventory status distribution", e);
        }
        return map;
    }

    @Override
    public List<ChartPoint> getTopPackagingServices(int limit) {
        List<ChartPoint> list = new ArrayList<>();
        String sql = "SELECT ps.service_name, COUNT(oi.order_item_id) AS usage_count, COALESCE(SUM(oi.total_price), 0) AS revenue " +
                     "FROM packaging_services ps " +
                     "LEFT JOIN order_items oi ON ps.service_id = oi.service_id " +
                     "GROUP BY ps.service_id, ps.service_name " +
                     "ORDER BY usage_count DESC, revenue DESC " +
                     "LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new ChartPoint(rs.getString("service_name"), rs.getDouble("usage_count"), "$" + rs.getBigDecimal("revenue")));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error querying top packaging services", e);
        }
        return list;
    }

    @Override
    public List<Order> getRecentOrders(int limit) {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT o.*, c.company_name, c.contact_person, c.email, c.phone " +
                     "FROM orders o " +
                     "JOIN customers c ON o.customer_id = c.customer_id " +
                     "ORDER BY o.order_id DESC " +
                     "LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Order o = new Order();
                    o.setOrderId(rs.getInt("order_id"));
                    o.setOrderNumber(rs.getString("order_number"));
                    o.setCustomerId(rs.getInt("customer_id"));
                    o.setOrderDate(rs.getDate("order_date"));
                    o.setStatus(OrderStatus.fromString(rs.getString("status")));
                    o.setSubtotal(rs.getBigDecimal("subtotal"));
                    o.setTotalCost(rs.getBigDecimal("total_cost"));
                    o.setNotes(rs.getString("notes"));
                    o.setCreatedAt(rs.getTimestamp("created_at"));
                    o.setCompanyName(rs.getString("company_name"));
                    o.setContactPerson(rs.getString("contact_person"));
                    list.add(o);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error querying recent orders", e);
        }
        return list;
    }

    @Override
    public List<InventoryItem> getLowStockItems(int limit) {
        List<InventoryItem> list = new ArrayList<>();
        String sql = "SELECT * FROM inventory WHERE quantity_available <= reorder_level " +
                     "ORDER BY quantity_available ASC " +
                     "LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new InventoryItem(
                            rs.getInt("inventory_id"),
                            rs.getString("material_name"),
                            rs.getString("material_code"),
                            rs.getInt("quantity_available"),
                            rs.getInt("quantity_reserved"),
                            rs.getInt("reorder_level"),
                            rs.getString("unit"),
                            rs.getTimestamp("updated_at")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error querying low stock alerts", e);
        }
        return list;
    }

    @Override
    public DashboardStats getComprehensiveStats(String dateFilter, Date startDate, Date endDate) {
        DashboardStats stats = new DashboardStats();
        stats.setDateFilter(dateFilter != null ? dateFilter : "all");
        stats.setLastUpdated(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm:ss a")));

        stats.setTotalOrders(getTotalOrders(dateFilter, startDate, endDate));
        stats.setPendingOrders(getPendingOrders());
        stats.setProcessingOrders(getProcessingOrders());
        stats.setCompletedOrders(getCompletedOrders());
        stats.setTotalCustomers(getTotalCustomers());
        stats.setTotalProducts(getTotalProducts());
        stats.setLowStockItemsCount(getLowStockCount());
        stats.setTotalRevenue(getTotalRevenue(dateFilter, startDate, endDate));

        stats.setOrdersByStatus(getOrdersByStatus(dateFilter, startDate, endDate));
        stats.setMonthlyOrders(getMonthlyOrders());
        stats.setMonthlyRevenue(getMonthlyRevenue());
        stats.setInventoryStatusBreakdown(getInventoryStatus());
        stats.setTopPackagingServices(getTopPackagingServices(5));

        stats.setRecentOrders(getRecentOrders(8));
        stats.setLowStockItems(getLowStockItems(5));

        return stats;
    }
}
