package com.packflow.service;

import com.packflow.dao.ReportDAO;
import com.packflow.dao.impl.ReportDAOImpl;
import com.packflow.model.OrderStatus;
import com.packflow.model.ReportRow;

import java.sql.Date;
import java.util.List;

/**
 * Service generating operational, inventory, revenue, and client reports via SQL aggregations.
 */
public class ReportService {

    private final ReportDAO reportDAO;

    public ReportService() {
        this.reportDAO = new ReportDAOImpl();
    }

    public ReportService(ReportDAO reportDAO) {
        this.reportDAO = reportDAO;
    }

    public List<ReportRow> getOrderReport(Date fromDate, Date toDate, Integer customerId, OrderStatus status) {
        return reportDAO.getOrderReport(fromDate, toDate, customerId, status);
    }

    public List<ReportRow> getRevenueReport(Date fromDate, Date toDate, Integer customerId) {
        return reportDAO.getRevenueReport(fromDate, toDate, customerId);
    }

    public List<ReportRow> getInventoryReport(String stockFilter) {
        return reportDAO.getInventoryReport(stockFilter);
    }

    public List<ReportRow> getCustomerReport() {
        return reportDAO.getCustomerReport();
    }

    public List<ReportRow> getServiceUsageReport(Date fromDate, Date toDate) {
        return reportDAO.getServiceUsageReport(fromDate, toDate);
    }
}
