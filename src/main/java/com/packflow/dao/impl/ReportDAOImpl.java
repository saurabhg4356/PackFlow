package com.packflow.dao.impl;

import com.packflow.dao.ReportDAO;
import com.packflow.exception.DatabaseException;
import com.packflow.model.OrderStatus;
import com.packflow.model.ReportRow;
import com.packflow.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReportDAOImpl implements ReportDAO {

    @Override
    public List<ReportRow> getOrderReport(Date fromDate, Date toDate, Integer customerId, OrderStatus status) {
        List<ReportRow> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT o.order_number, c.company_name, o.status, o.order_date, " +
                "COUNT(oi.order_item_id) as item_count, COALESCE(SUM(oi.quantity), 0) as total_qty, o.total_cost " +
                "FROM orders o " +
                "JOIN customers c ON o.customer_id = c.customer_id " +
                "LEFT JOIN order_items oi ON o.order_id = oi.order_id " +
                "WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        if (fromDate != null) {
            sql.append("AND o.order_date >= ? ");
            params.add(fromDate);
        }
        if (toDate != null) {
            sql.append("AND o.order_date <= ? ");
            params.add(toDate);
        }
        if (customerId != null && customerId > 0) {
            sql.append("AND o.customer_id = ? ");
            params.add(customerId);
        }
        if (status != null) {
            sql.append("AND o.status = ? ");
            params.add(status.name());
        }

        sql.append("GROUP BY o.order_id, o.order_number, c.company_name, o.status, o.order_date, o.total_cost ");
        sql.append("ORDER BY o.order_date DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ReportRow row = new ReportRow();
                    row.setCol1(rs.getString("order_number"));
                    row.setCol2(rs.getString("company_name"));
                    row.setCol3(rs.getString("status"));
                    row.setDateCol(rs.getDate("order_date"));
                    row.setCountVal(rs.getInt("item_count"));
                    row.setQuantityVal(rs.getInt("total_qty"));
                    row.setAmountVal(rs.getBigDecimal("total_cost"));
                    list.add(row);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error generating order report", e);
        }
        return list;
    }

    @Override
    public List<ReportRow> getRevenueReport(Date fromDate, Date toDate, Integer customerId) {
        List<ReportRow> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT DATE_FORMAT(o.order_date, '%Y-%m') as ym_label, c.company_name, " +
                "COUNT(DISTINCT o.order_id) as order_count, SUM(o.total_cost) as total_revenue " +
                "FROM orders o " +
                "JOIN customers c ON o.customer_id = c.customer_id " +
                "WHERE o.status != 'CANCELLED' "
        );
        List<Object> params = new ArrayList<>();

        if (fromDate != null) {
            sql.append("AND o.order_date >= ? ");
            params.add(fromDate);
        }
        if (toDate != null) {
            sql.append("AND o.order_date <= ? ");
            params.add(toDate);
        }
        if (customerId != null && customerId > 0) {
            sql.append("AND o.customer_id = ? ");
            params.add(customerId);
        }

        sql.append("GROUP BY DATE_FORMAT(o.order_date, '%Y-%m'), c.customer_id, c.company_name ");
        sql.append("ORDER BY ym_label DESC, total_revenue DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ReportRow row = new ReportRow();
                    row.setCol1(rs.getString("ym_label"));
                    row.setCol2(rs.getString("company_name"));
                    row.setCountVal(rs.getInt("order_count"));
                    row.setAmountVal(rs.getBigDecimal("total_revenue"));
                    list.add(row);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error generating revenue report", e);
        }
        return list;
    }

    @Override
    public List<ReportRow> getInventoryReport(String stockFilter) {
        List<ReportRow> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT material_code, material_name, unit, quantity_available, quantity_reserved, reorder_level " +
                "FROM inventory WHERE 1=1 "
        );

        if ("LOW".equalsIgnoreCase(stockFilter)) {
            sql.append("AND quantity_available > 0 AND quantity_available <= reorder_level ");
        } else if ("OUT".equalsIgnoreCase(stockFilter)) {
            sql.append("AND quantity_available <= 0 ");
        }

        sql.append("ORDER BY quantity_available ASC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString());
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                ReportRow row = new ReportRow();
                row.setCol1(rs.getString("material_code"));
                row.setCol2(rs.getString("material_name"));
                row.setCol3(rs.getString("unit"));
                row.setQuantityVal(rs.getInt("quantity_available"));
                row.setCountVal(rs.getInt("quantity_reserved"));
                list.add(row);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error generating inventory report", e);
        }
        return list;
    }

    @Override
    public List<ReportRow> getCustomerReport() {
        List<ReportRow> list = new ArrayList<>();
        String sql = "SELECT c.company_name, c.contact_person, c.email, " +
                     "COUNT(o.order_id) as total_orders, " +
                     "COALESCE(SUM(CASE WHEN o.status != 'CANCELLED' THEN o.total_cost ELSE 0 END), 0) as total_spend " +
                     "FROM customers c " +
                     "LEFT JOIN orders o ON c.customer_id = o.customer_id " +
                     "GROUP BY c.customer_id, c.company_name, c.contact_person, c.email " +
                     "ORDER BY total_spend DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                ReportRow row = new ReportRow();
                row.setCol1(rs.getString("company_name"));
                row.setCol2(rs.getString("contact_person"));
                row.setCol3(rs.getString("email"));
                row.setCountVal(rs.getInt("total_orders"));
                row.setAmountVal(rs.getBigDecimal("total_spend"));
                list.add(row);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error generating customer report", e);
        }
        return list;
    }

    @Override
    public List<ReportRow> getServiceUsageReport(Date fromDate, Date toDate) {
        List<ReportRow> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT ps.service_name, ps.base_price, ps.status, " +
                "COUNT(oi.order_item_id) as times_used, " +
                "COALESCE(SUM(oi.quantity), 0) as units_processed, " +
                "COALESCE(SUM(oi.total_price), 0) as total_revenue " +
                "FROM packaging_services ps " +
                "LEFT JOIN order_items oi ON ps.service_id = oi.service_id " +
                "LEFT JOIN orders o ON oi.order_id = o.order_id " +
                "WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        if (fromDate != null) {
            sql.append("AND o.order_date >= ? ");
            params.add(fromDate);
        }
        if (toDate != null) {
            sql.append("AND o.order_date <= ? ");
            params.add(toDate);
        }

        sql.append("GROUP BY ps.service_id, ps.service_name, ps.base_price, ps.status ");
        sql.append("ORDER BY total_revenue DESC, times_used DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ReportRow row = new ReportRow();
                    row.setCol1(rs.getString("service_name"));
                    row.setCol2("$" + rs.getBigDecimal("base_price"));
                    row.setCol3(rs.getString("status"));
                    row.setCountVal(rs.getInt("times_used"));
                    row.setQuantityVal(rs.getInt("units_processed"));
                    row.setAmountVal(rs.getBigDecimal("total_revenue"));
                    list.add(row);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error generating service usage report", e);
        }
        return list;
    }
}
