package com.packflow.controller;

import com.packflow.exception.InsufficientInventoryException;
import com.packflow.exception.InvalidOrderException;
import com.packflow.exception.InvalidStatusTransitionException;
import com.packflow.model.Customer;
import com.packflow.model.CustomerUser;
import com.packflow.model.Order;
import com.packflow.model.OrderItem;
import com.packflow.model.OrderStatus;
import com.packflow.model.PackagingServiceItem;
import com.packflow.model.Product;
import com.packflow.model.Role;
import com.packflow.model.User;
import com.packflow.service.CustomerService;
import com.packflow.service.OrderService;
import com.packflow.service.PackagingServiceService;
import com.packflow.service.ProductService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controller handling Order creation, listing, status transitions, and lifecycle actions.
 */
@WebServlet(name = "OrderServlet", urlPatterns = {"/orders"})
public class OrderServlet extends HttpServlet {

    private OrderService orderService;
    private CustomerService customerService;
    private ProductService productService;
    private PackagingServiceService packagingServiceService;

    @Override
    public void init() {
        this.orderService = new OrderService();
        this.customerService = new CustomerService();
        this.productService = new ProductService();
        this.packagingServiceService = new PackagingServiceService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (User) session.getAttribute("currentUser");

        String action = req.getParameter("action");
        if (action == null) {
            action = "list";
        }

        switch (action) {
            case "view" -> viewOrderDetails(req, resp, currentUser);
            case "new" -> showCreateForm(req, resp, currentUser);
            default -> listOrders(req, resp, currentUser);
        }
    }

    private void listOrders(HttpServletRequest req, HttpServletResponse resp, User currentUser)
            throws ServletException, IOException {

        int page = 1;
        int pageSize = 10;
        try {
            String p = req.getParameter("page");
            if (p != null) page = Math.max(1, Integer.parseInt(p));
        } catch (NumberFormatException ignored) {
        }

        String search = req.getParameter("search");
        String statusStr = req.getParameter("status");
        OrderStatus status = (statusStr != null && !statusStr.isEmpty()) ? OrderStatus.fromString(statusStr) : null;

        Integer customerFilter = null;
        if (currentUser instanceof CustomerUser cu) {
            customerFilter = cu.getCustomerId();
        } else {
            String cId = req.getParameter("customerId");
            if (cId != null && !cId.isEmpty()) {
                try {
                    customerFilter = Integer.parseInt(cId);
                } catch (NumberFormatException ignored) {
                }
            }
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

        List<Order> orders = orderService.getPaginatedOrders(page, pageSize, search, customerFilter, status, fromDate, toDate);
        int totalOrders = orderService.getTotalOrderCount(search, customerFilter, status, fromDate, toDate);
        int totalPages = (int) Math.ceil((double) totalOrders / pageSize);

        req.setAttribute("orders", orders);
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("totalOrders", totalOrders);
        req.setAttribute("search", search);
        req.setAttribute("selectedStatus", status != null ? status.name() : "");
        req.setAttribute("selectedCustomer", customerFilter);
        req.setAttribute("fromDate", fromDate != null ? fromDate.toString() : "");
        req.setAttribute("toDate", toDate != null ? toDate.toString() : "");

        if (currentUser.getRole() != Role.CUSTOMER) {
            req.setAttribute("customersList", customerService.getAllCustomers());
        }

        req.getRequestDispatcher("/orders.jsp").forward(req, resp);
    }

    private void viewOrderDetails(HttpServletRequest req, HttpServletResponse resp, User currentUser)
            throws ServletException, IOException {

        int orderId = 0;
        try {
            orderId = Integer.parseInt(req.getParameter("id"));
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/orders");
            return;
        }

        Optional<Order> opt = orderService.getOrderById(orderId);
        if (opt.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/orders?error=not_found");
            return;
        }

        Order order = opt.get();

        // Customer access authorization check
        if (currentUser instanceof CustomerUser cu) {
            if (cu.getCustomerId() == null || !cu.getCustomerId().equals(order.getCustomerId())) {
                resp.sendRedirect(req.getContextPath() + "/unauthorized");
                return;
            }
        }

        req.setAttribute("order", order);
        req.setAttribute("allowedTransitions", order.getStatus().getAllowedNextStatuses());

        String msg = req.getParameter("msg");
        if ("approved".equals(msg)) req.setAttribute("successMessage", "Order approved and packaging materials reserved successfully!");
        else if ("created".equals(msg)) req.setAttribute("successMessage", "Order created successfully!");
        else if ("processing".equals(msg)) req.setAttribute("successMessage", "Order moved to Processing. Packaging task initiated.");
        else if ("cancelled".equals(msg)) req.setAttribute("successMessage", "Order cancelled and any reserved materials released.");

        String error = req.getParameter("error");
        if (error != null) req.setAttribute("errorMessage", error);

        req.getRequestDispatcher("/order-details.jsp").forward(req, resp);
    }

    private void showCreateForm(HttpServletRequest req, HttpServletResponse resp, User currentUser)
            throws ServletException, IOException {

        List<Customer> customers;
        List<Product> products;

        if (currentUser instanceof CustomerUser cu) {
            customers = new ArrayList<>();
            customerService.getCustomerById(cu.getCustomerId()).ifPresent(customers::add);
            products = productService.getProductsByCustomerId(cu.getCustomerId());
        } else {
            customers = customerService.getAllCustomers();
            products = productService.getAllProducts();
        }

        List<PackagingServiceItem> services = packagingServiceService.getActiveServices();

        req.setAttribute("customers", customers);
        req.setAttribute("products", products);
        req.setAttribute("services", services);

        req.getRequestDispatcher("/create-order.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (User) session.getAttribute("currentUser");

        String action = req.getParameter("action");
        if (action == null) {
            resp.sendRedirect(req.getContextPath() + "/orders");
            return;
        }

        switch (action) {
            case "create" -> handleCreateOrder(req, resp, currentUser);
            case "approve" -> handleApproveOrder(req, resp, currentUser);
            case "process" -> handleStartProcessing(req, resp, currentUser);
            case "cancel" -> handleCancelOrder(req, resp, currentUser);
            case "to_qc" -> handleSendToQC(req, resp, currentUser);
            default -> resp.sendRedirect(req.getContextPath() + "/orders");
        }
    }

    private void handleCreateOrder(HttpServletRequest req, HttpServletResponse resp, User currentUser)
            throws ServletException, IOException {

        try {
            int customerId;
            if (currentUser instanceof CustomerUser cu) {
                customerId = cu.getCustomerId();
            } else {
                customerId = Integer.parseInt(req.getParameter("customerId"));
            }

            String notes = req.getParameter("notes");
            String[] productIds = req.getParameterValues("productId[]");
            String[] serviceIds = req.getParameterValues("serviceId[]");
            String[] quantities = req.getParameterValues("quantity[]");

            if (productIds == null || serviceIds == null || quantities == null || productIds.length == 0) {
                throw new InvalidOrderException("At least one packaging item is required.");
            }

            List<OrderItem> items = new ArrayList<>();
            for (int i = 0; i < productIds.length; i++) {
                if (productIds[i] == null || productIds[i].isEmpty()) continue;
                int pId = Integer.parseInt(productIds[i]);
                int sId = Integer.parseInt(serviceIds[i]);
                int qty = Integer.parseInt(quantities[i]);

                OrderItem item = new OrderItem();
                item.setProductId(pId);
                item.setServiceId(sId);
                item.setQuantity(qty);
                items.add(item);
            }

            Order order = new Order();
            order.setCustomerId(customerId);
            order.setNotes(notes);

            Order created = orderService.createOrder(order, items);
            resp.sendRedirect(req.getContextPath() + "/orders?action=view&id=" + created.getOrderId() + "&msg=created");
        } catch (Exception e) {
            req.setAttribute("errorMessage", "Failed to create order: " + e.getMessage());
            showCreateForm(req, resp, currentUser);
        }
    }

    private void handleApproveOrder(HttpServletRequest req, HttpServletResponse resp, User currentUser)
            throws IOException {

        if (currentUser.getRole() == Role.CUSTOMER) {
            resp.sendRedirect(req.getContextPath() + "/unauthorized");
            return;
        }

        int orderId = Integer.parseInt(req.getParameter("orderId"));
        try {
            orderService.approveOrderAndReserveInventory(orderId);
            resp.sendRedirect(req.getContextPath() + "/orders?action=view&id=" + orderId + "&msg=approved");
        } catch (InsufficientInventoryException e) {
            resp.sendRedirect(req.getContextPath() + "/orders?action=view&id=" + orderId +
                    "&error=" + java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
        } catch (Exception e) {
            resp.sendRedirect(req.getContextPath() + "/orders?action=view&id=" + orderId +
                    "&error=" + java.net.URLEncoder.encode("Error approving order: " + e.getMessage(), "UTF-8"));
        }
    }

    private void handleStartProcessing(HttpServletRequest req, HttpServletResponse resp, User currentUser)
            throws IOException {

        if (currentUser.getRole() == Role.CUSTOMER) {
            resp.sendRedirect(req.getContextPath() + "/unauthorized");
            return;
        }

        int orderId = Integer.parseInt(req.getParameter("orderId"));
        try {
            orderService.startProcessing(orderId, currentUser.getUserId());
            resp.sendRedirect(req.getContextPath() + "/orders?action=view&id=" + orderId + "&msg=processing");
        } catch (Exception e) {
            resp.sendRedirect(req.getContextPath() + "/orders?action=view&id=" + orderId +
                    "&error=" + java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
        }
    }

    private void handleSendToQC(HttpServletRequest req, HttpServletResponse resp, User currentUser)
            throws IOException {

        if (currentUser.getRole() == Role.CUSTOMER) {
            resp.sendRedirect(req.getContextPath() + "/unauthorized");
            return;
        }

        int orderId = Integer.parseInt(req.getParameter("orderId"));
        try {
            orderService.sendToQualityCheck(orderId);
            resp.sendRedirect(req.getContextPath() + "/orders?action=view&id=" + orderId);
        } catch (Exception e) {
            resp.sendRedirect(req.getContextPath() + "/orders?action=view&id=" + orderId +
                    "&error=" + java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
        }
    }

    private void handleCancelOrder(HttpServletRequest req, HttpServletResponse resp, User currentUser)
            throws IOException {

        int orderId = Integer.parseInt(req.getParameter("orderId"));
        String reason = req.getParameter("reason");

        try {
            orderService.cancelOrder(orderId, reason);
            resp.sendRedirect(req.getContextPath() + "/orders?action=view&id=" + orderId + "&msg=cancelled");
        } catch (Exception e) {
            resp.sendRedirect(req.getContextPath() + "/orders?action=view&id=" + orderId +
                    "&error=" + java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
        }
    }
}
