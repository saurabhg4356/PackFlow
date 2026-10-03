package com.packflow.dao;

import com.packflow.model.OrderStatus;
import com.packflow.model.ReportRow;

import java.sql.Date;
import java.util.List;

/**
 * Data Access Object Interface for Business Reports using SQL Aggregations and Group-By queries.
 */
public interface ReportDAO {
    List<ReportRow> getOrderReport(Date fromDate, Date toDate, Integer customerId, OrderStatus status);
    List<ReportRow> getRevenueReport(Date fromDate, Date toDate, Integer customerId);
    List<ReportRow> getInventoryReport(String stockFilter);
    List<ReportRow> getCustomerReport();
    List<ReportRow> getServiceUsageReport(Date fromDate, Date toDate);
}
