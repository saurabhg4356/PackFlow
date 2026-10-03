package com.packflow.controller;

import com.packflow.model.OrderStatus;
import com.packflow.model.ReportRow;
import com.packflow.model.Role;
import com.packflow.model.User;
import com.packflow.service.CustomerService;
import com.packflow.service.ReportService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Date;
import java.util.List;

/**
 * Controller generating live SQL-aggregated business and operational reports.
 */
@WebServlet(name = "ReportServlet", urlPatterns = {"/reports"})
public class ReportServlet extends HttpServlet {

    private ReportService reportService;
    private CustomerService customerService;

    @Override
    public void init() {
        this.reportService = new ReportService();
        this.customerService = new CustomerService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (User) session.getAttribute("currentUser");

        if (currentUser.getRole() == Role.CUSTOMER) {
            resp.sendRedirect(req.getContextPath() + "/unauthorized");
            return;
        }

        String type = req.getParameter("type");
        if (type == null || type.trim().isEmpty()) {
            type = "order";
        }

        Date fromDate = null;
        Date toDate = null;
        try {
            String from = req.getParameter("fromDate");
            String to = req.getParameter("toDate");
            if (from != null && !from.isEmpty()) fromDate = Date.valueOf(from);
            if (to != null && !to.isEmpty()) toDate = Date.valueOf(to);
        } catch (Exception ignored) {
        }

        Integer customerId = null;
        try {
            String cStr = req.getParameter("customerId");
            if (cStr != null && !cStr.isEmpty()) customerId = Integer.parseInt(cStr);
        } catch (Exception ignored) {
        }

        String statusStr = req.getParameter("status");
        OrderStatus status = (statusStr != null && !statusStr.isEmpty()) ? OrderStatus.fromString(statusStr) : null;
        String stockFilter = req.getParameter("stockFilter");

        List<ReportRow> rows = switch (type.toLowerCase()) {
            case "revenue" -> reportService.getRevenueReport(fromDate, toDate, customerId);
            case "inventory" -> reportService.getInventoryReport(stockFilter);
            case "customer" -> reportService.getCustomerReport();
            case "service" -> reportService.getServiceUsageReport(fromDate, toDate);
            default -> reportService.getOrderReport(fromDate, toDate, customerId, status);
        };

        req.setAttribute("reportType", type);
        req.setAttribute("rows", rows);
        req.setAttribute("fromDate", fromDate != null ? fromDate.toString() : "");
        req.setAttribute("toDate", toDate != null ? toDate.toString() : "");
        req.setAttribute("selectedCustomer", customerId);
        req.setAttribute("selectedStatus", status != null ? status.name() : "");
        req.setAttribute("stockFilter", stockFilter != null ? stockFilter : "");
        req.setAttribute("customersList", customerService.getAllCustomers());

        req.getRequestDispatcher("/reports.jsp").forward(req, resp);
    }
}
