package com.packflow.controller;

import com.packflow.model.QualityCheck;
import com.packflow.model.QualityStatus;
import com.packflow.model.Role;
import com.packflow.model.User;
import com.packflow.service.OrderService;
import com.packflow.service.QualityCheckService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * Controller managing quality inspections and compliance certification before dispatch.
 */
@WebServlet(name = "QualityCheckServlet", urlPatterns = {"/quality-checks"})
public class QualityCheckServlet extends HttpServlet {

    private QualityCheckService qualityCheckService;
    private OrderService orderService;

    @Override
    public void init() {
        this.qualityCheckService = new QualityCheckService();
        this.orderService = new OrderService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (User) session.getAttribute("currentUser");

        if (currentUser.getRole() == Role.CUSTOMER) {
            resp.sendRedirect(req.getContextPath() + "/unauthorized");
            return;
        }

        int page = 1;
        int pageSize = 10;
        try {
            String p = req.getParameter("page");
            if (p != null) page = Math.max(1, Integer.parseInt(p));
        } catch (NumberFormatException ignored) {
        }

        String statusStr = req.getParameter("status");
        QualityStatus status = (statusStr != null && !statusStr.isEmpty()) ? QualityStatus.fromString(statusStr) : null;

        List<QualityCheck> checks = qualityCheckService.getPaginatedQualityChecks(page, pageSize, status);
        int total = qualityCheckService.getTotalCount(status);
        int totalPages = (int) Math.ceil((double) total / pageSize);

        req.setAttribute("checks", checks);
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("totalChecks", total);
        req.setAttribute("selectedStatus", status != null ? status.name() : "");

        String msg = req.getParameter("msg");
        if ("recorded".equals(msg)) req.setAttribute("successMessage", "Quality inspection recorded successfully.");

        req.getRequestDispatcher("/quality-checks.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (User) session.getAttribute("currentUser");

        if (currentUser.getRole() == Role.CUSTOMER) {
            resp.sendRedirect(req.getContextPath() + "/unauthorized");
            return;
        }

        try {
            int orderId = Integer.parseInt(req.getParameter("orderId"));
            int passedQty = Integer.parseInt(req.getParameter("quantityPassed"));
            int failedQty = Integer.parseInt(req.getParameter("quantityFailed"));
            String remarks = req.getParameter("remarks");
            QualityStatus status = QualityStatus.fromString(req.getParameter("status"));

            orderService.recordQualityCheck(orderId, currentUser.getUserId(), passedQty, failedQty, remarks, status);
            resp.sendRedirect(req.getContextPath() + "/orders?action=view&id=" + orderId + "&msg=qc_recorded");
        } catch (Exception e) {
            resp.sendRedirect(req.getContextPath() + "/quality-checks?error=" + java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
        }
    }
}
