package com.packflow.controller;

import com.google.gson.Gson;
import com.packflow.model.CustomerUser;
import com.packflow.model.DashboardStats;
import com.packflow.model.Order;
import com.packflow.model.Role;
import com.packflow.model.User;
import com.packflow.service.DashboardService;
import com.packflow.service.OrderService;

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
 * Controller rendering live database-driven statistics and Chart.js analytics.
 * Strictly guarantees NO HARDCODED OR FAKE VALUES.
 */
@WebServlet(name = "DashboardServlet", urlPatterns = {"/dashboard"})
public class DashboardServlet extends HttpServlet {

    private DashboardService dashboardService;
    private OrderService orderService;
    private Gson gson;

    @Override
    public void init() {
        this.dashboardService = new DashboardService();
        this.orderService = new OrderService();
        this.gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (User) session.getAttribute("currentUser");

        String dateFilter = req.getParameter("dateFilter");
        if (dateFilter == null || dateFilter.trim().isEmpty()) {
            dateFilter = "all";
        }

        Date startDate = null;
        Date endDate = null;
        if ("custom".equalsIgnoreCase(dateFilter)) {
            try {
                String startStr = req.getParameter("startDate");
                String endStr = req.getParameter("endDate");
                if (startStr != null && !startStr.isEmpty()) {
                    startDate = Date.valueOf(startStr);
                }
                if (endStr != null && !endStr.isEmpty()) {
                    endDate = Date.valueOf(endStr);
                }
            } catch (Exception ignored) {
            }
        }

        // Retrieve real metrics calculated via MySQL
        DashboardStats stats = dashboardService.getDashboardStatistics(dateFilter, startDate, endDate);

        // If Customer user, load customer's specific orders
        if (currentUser instanceof CustomerUser cu && cu.getCustomerId() != null) {
            List<Order> customerOrders = orderService.getOrdersByCustomerId(cu.getCustomerId());
            req.setAttribute("customerOrders", customerOrders);
        }

        // Serialized JSON for Chart.js rendering
        String ordersByStatusJson = gson.toJson(stats.getOrdersByStatus());
        String monthlyOrdersJson = gson.toJson(stats.getMonthlyOrders());
        String monthlyRevenueJson = gson.toJson(stats.getMonthlyRevenue());
        String inventoryStatusJson = gson.toJson(stats.getInventoryStatusBreakdown());
        String topServicesJson = gson.toJson(stats.getTopPackagingServices());

        // Check if AJAX request
        String format = req.getParameter("format");
        if ("json".equalsIgnoreCase(format) || "XMLHttpRequest".equals(req.getHeader("X-Requested-With"))) {
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write(gson.toJson(stats));
            return;
        }

        // Attach attributes to request scope
        req.setAttribute("stats", stats);
        req.setAttribute("selectedFilter", dateFilter);
        req.setAttribute("startDate", startDate != null ? startDate.toString() : "");
        req.setAttribute("endDate", endDate != null ? endDate.toString() : "");

        req.setAttribute("ordersByStatusJson", ordersByStatusJson);
        req.setAttribute("monthlyOrdersJson", monthlyOrdersJson);
        req.setAttribute("monthlyRevenueJson", monthlyRevenueJson);
        req.setAttribute("inventoryStatusJson", inventoryStatusJson);
        req.setAttribute("topServicesJson", topServicesJson);

        req.getRequestDispatcher("/dashboard.jsp").forward(req, resp);
    }
}
