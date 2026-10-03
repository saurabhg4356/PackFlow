package com.packflow.controller;

import com.packflow.model.DispatchRecord;
import com.packflow.model.DispatchStatus;
import com.packflow.model.Role;
import com.packflow.model.User;
import com.packflow.service.DispatchService;
import com.packflow.service.OrderService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * Controller managing dispatches, courier assignment, tracking numbers, and delivery confirmation.
 */
@WebServlet(name = "DispatchServlet", urlPatterns = {"/dispatches"})
public class DispatchServlet extends HttpServlet {

    private DispatchService dispatchService;
    private OrderService orderService;

    @Override
    public void init() {
        this.dispatchService = new DispatchService();
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
        DispatchStatus status = (statusStr != null && !statusStr.isEmpty()) ? DispatchStatus.fromString(statusStr) : null;

        List<DispatchRecord> dispatches = dispatchService.getPaginatedDispatches(page, pageSize, status);
        int total = dispatchService.getTotalCount(status);
        int totalPages = (int) Math.ceil((double) total / pageSize);

        req.setAttribute("dispatches", dispatches);
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("totalDispatches", total);
        req.setAttribute("selectedStatus", status != null ? status.name() : "");

        String msg = req.getParameter("msg");
        if ("dispatched".equals(msg)) req.setAttribute("successMessage", "Order marked as dispatched with tracking number.");
        else if ("delivered".equals(msg)) req.setAttribute("successMessage", "Delivery confirmed successfully.");

        req.getRequestDispatcher("/dispatches.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (User) session.getAttribute("currentUser");

        if (currentUser.getRole() == Role.CUSTOMER) {
            resp.sendRedirect(req.getContextPath() + "/unauthorized");
            return;
        }

        String action = req.getParameter("action");
        if ("dispatch".equalsIgnoreCase(action)) {
            try {
                int orderId = Integer.parseInt(req.getParameter("orderId"));
                String partner = req.getParameter("deliveryPartner");
                String tracking = req.getParameter("trackingNumber");

                orderService.dispatchOrder(orderId, partner, tracking);
                resp.sendRedirect(req.getContextPath() + "/orders?action=view&id=" + orderId + "&msg=dispatched");
            } catch (Exception e) {
                resp.sendRedirect(req.getContextPath() + "/dispatches?error=" + java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
            }
        } else if ("deliver".equalsIgnoreCase(action)) {
            try {
                int orderId = Integer.parseInt(req.getParameter("orderId"));
                orderService.markOrderDelivered(orderId);
                resp.sendRedirect(req.getContextPath() + "/orders?action=view&id=" + orderId + "&msg=delivered");
            } catch (Exception e) {
                resp.sendRedirect(req.getContextPath() + "/dispatches?error=" + java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
            }
        } else {
            resp.sendRedirect(req.getContextPath() + "/dispatches");
        }
    }
}
