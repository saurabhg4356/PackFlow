package com.packflow.controller;

import com.packflow.model.Customer;
import com.packflow.model.Role;
import com.packflow.model.User;
import com.packflow.service.CustomerService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * Controller managing customer clients, contact info, and addresses.
 */
@WebServlet(name = "CustomerServlet", urlPatterns = {"/customers"})
public class CustomerServlet extends HttpServlet {

    private CustomerService customerService;

    @Override
    public void init() {
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

        int page = 1;
        int pageSize = 10;
        try {
            String p = req.getParameter("page");
            if (p != null) page = Math.max(1, Integer.parseInt(p));
        } catch (NumberFormatException ignored) {
        }

        String search = req.getParameter("search");
        List<Customer> customers = customerService.getPaginatedCustomers(page, pageSize, search);
        int totalCustomers = customerService.getTotalCustomerCount(search);
        int totalPages = (int) Math.ceil((double) totalCustomers / pageSize);

        req.setAttribute("customers", customers);
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("totalCustomers", totalCustomers);
        req.setAttribute("search", search);

        String msg = req.getParameter("msg");
        if ("saved".equals(msg)) req.setAttribute("successMessage", "Customer profile saved successfully.");
        else if ("deleted".equals(msg)) req.setAttribute("successMessage", "Customer deleted successfully.");

        req.getRequestDispatcher("/customers.jsp").forward(req, resp);
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
        if ("delete".equalsIgnoreCase(action)) {
            int customerId = Integer.parseInt(req.getParameter("customerId"));
            customerService.deleteCustomer(customerId);
            resp.sendRedirect(req.getContextPath() + "/customers?msg=deleted");
            return;
        }

        try {
            int customerId = 0;
            String idStr = req.getParameter("customerId");
            if (idStr != null && !idStr.isEmpty()) customerId = Integer.parseInt(idStr);

            Customer customer = new Customer();
            customer.setCustomerId(customerId);
            customer.setCompanyName(req.getParameter("companyName"));
            customer.setContactPerson(req.getParameter("contactPerson"));
            customer.setPhone(req.getParameter("phone"));
            customer.setEmail(req.getParameter("email"));
            customer.setAddress(req.getParameter("address"));

            customerService.saveCustomer(customer);
            resp.sendRedirect(req.getContextPath() + "/customers?msg=saved");
        } catch (Exception e) {
            resp.sendRedirect(req.getContextPath() + "/customers?error=" + java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
        }
    }
}
