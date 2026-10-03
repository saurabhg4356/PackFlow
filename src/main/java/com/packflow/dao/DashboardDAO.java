package com.packflow.dao;

import com.packflow.model.ChartPoint;
import com.packflow.model.DashboardStats;
import com.packflow.model.InventoryItem;
import com.packflow.model.Order;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object Interface for Live Database-Backed Dashboard Analytics.
 * Strictly calculates all metrics and charts directly from MySQL queries.
 */
public interface DashboardDAO {
    int getTotalOrders(String dateFilter, Date startDate, Date endDate);
    int getPendingOrders();
    int getProcessingOrders();
    int getCompletedOrders();
    int getTotalCustomers();
    int getTotalProducts();
    int getLowStockCount();
    BigDecimal getTotalRevenue(String dateFilter, Date startDate, Date endDate);

    Map<String, Integer> getOrdersByStatus(String dateFilter, Date startDate, Date endDate);
    List<ChartPoint> getMonthlyOrders();
    List<ChartPoint> getMonthlyRevenue();
    Map<String, Integer> getInventoryStatus();
    List<ChartPoint> getTopPackagingServices(int limit);

    List<Order> getRecentOrders(int limit);
    List<InventoryItem> getLowStockItems(int limit);

    DashboardStats getComprehensiveStats(String dateFilter, Date startDate, Date endDate);
}
