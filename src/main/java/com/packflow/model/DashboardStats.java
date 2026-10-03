package com.packflow.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregated dashboard metrics directly calculated from live database queries.
 * NO HARDCODED OR FAKE VALUES.
 */
public class DashboardStats {
    private int totalOrders;
    private int pendingOrders;
    private int processingOrders;
    private int completedOrders;
    private int totalCustomers;
    private int totalProducts;
    private int lowStockItemsCount;
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    // Charts data populated from real DB group-by queries
    private Map<String, Integer> ordersByStatus = new LinkedHashMap<>();
    private List<ChartPoint> monthlyOrders = new ArrayList<>();
    private List<ChartPoint> monthlyRevenue = new ArrayList<>();
    private Map<String, Integer> inventoryStatusBreakdown = new LinkedHashMap<>();
    private List<ChartPoint> topPackagingServices = new ArrayList<>();

    // Real table listings
    private List<Order> recentOrders = new ArrayList<>();
    private List<InventoryItem> lowStockItems = new ArrayList<>();

    // Metadata
    private String lastUpdated;
    private String dateFilter; // 'all', 'today', 'this_week', 'this_month', 'this_year', 'custom'

    public DashboardStats() {
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    public int getPendingOrders() {
        return pendingOrders;
    }

    public void setPendingOrders(int pendingOrders) {
        this.pendingOrders = pendingOrders;
    }

    public int getProcessingOrders() {
        return processingOrders;
    }

    public void setProcessingOrders(int processingOrders) {
        this.processingOrders = processingOrders;
    }

    public int getCompletedOrders() {
        return completedOrders;
    }

    public void setCompletedOrders(int completedOrders) {
        this.completedOrders = completedOrders;
    }

    public int getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(int totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

    public int getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(int totalProducts) {
        this.totalProducts = totalProducts;
    }

    public int getLowStockItemsCount() {
        return lowStockItemsCount;
    }

    public void setLowStockItemsCount(int lowStockItemsCount) {
        this.lowStockItemsCount = lowStockItemsCount;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
    }

    public Map<String, Integer> getOrdersByStatus() {
        return ordersByStatus;
    }

    public void setOrdersByStatus(Map<String, Integer> ordersByStatus) {
        this.ordersByStatus = ordersByStatus;
    }

    public List<ChartPoint> getMonthlyOrders() {
        return monthlyOrders;
    }

    public void setMonthlyOrders(List<ChartPoint> monthlyOrders) {
        this.monthlyOrders = monthlyOrders;
    }

    public List<ChartPoint> getMonthlyRevenue() {
        return monthlyRevenue;
    }

    public void setMonthlyRevenue(List<ChartPoint> monthlyRevenue) {
        this.monthlyRevenue = monthlyRevenue;
    }

    public Map<String, Integer> getInventoryStatusBreakdown() {
        return inventoryStatusBreakdown;
    }

    public void setInventoryStatusBreakdown(Map<String, Integer> inventoryStatusBreakdown) {
        this.inventoryStatusBreakdown = inventoryStatusBreakdown;
    }

    public List<ChartPoint> getTopPackagingServices() {
        return topPackagingServices;
    }

    public void setTopPackagingServices(List<ChartPoint> topPackagingServices) {
        this.topPackagingServices = topPackagingServices;
    }

    public List<Order> getRecentOrders() {
        return recentOrders;
    }

    public void setRecentOrders(List<Order> recentOrders) {
        this.recentOrders = recentOrders;
    }

    public List<InventoryItem> getLowStockItems() {
        return lowStockItems;
    }

    public void setLowStockItems(List<InventoryItem> lowStockItems) {
        this.lowStockItems = lowStockItems;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getDateFilter() {
        return dateFilter;
    }

    public void setDateFilter(String dateFilter) {
        this.dateFilter = dateFilter;
    }
}
