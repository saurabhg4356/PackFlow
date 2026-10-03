package com.packflow.controller;

import com.packflow.model.Customer;
import com.packflow.model.CustomerUser;
import com.packflow.model.Product;
import com.packflow.model.Role;
import com.packflow.model.User;
import com.packflow.service.CustomerService;
import com.packflow.service.ProductService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * Controller managing client products catalog.
 */
@WebServlet(name = "ProductServlet", urlPatterns = {"/products"})
public class ProductServlet extends HttpServlet {

    private ProductService productService;
    private CustomerService customerService;

    @Override
    public void init() {
        this.productService = new ProductService();
        this.customerService = new CustomerService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (User) session.getAttribute("currentUser");

        int page = 1;
        int pageSize = 10;
        try {
            String p = req.getParameter("page");
            if (p != null) page = Math.max(1, Integer.parseInt(p));
        } catch (NumberFormatException ignored) {
        }

        String search = req.getParameter("search");
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

        List<Product> products = productService.getPaginatedProducts(page, pageSize, search, customerFilter);
        int totalProducts = productService.getTotalProductCount(search, customerFilter);
        int totalPages = (int) Math.ceil((double) totalProducts / pageSize);

        req.setAttribute("products", products);
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("totalProducts", totalProducts);
        req.setAttribute("search", search);
        req.setAttribute("selectedCustomer", customerFilter);

        if (currentUser.getRole() != Role.CUSTOMER) {
            req.setAttribute("customersList", customerService.getAllCustomers());
        }

        String msg = req.getParameter("msg");
        if ("saved".equals(msg)) req.setAttribute("successMessage", "Product saved successfully.");
        else if ("deleted".equals(msg)) req.setAttribute("successMessage", "Product deleted successfully.");

        req.getRequestDispatcher("/products.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (User) session.getAttribute("currentUser");

        String action = req.getParameter("action");
        if ("delete".equalsIgnoreCase(action)) {
            int productId = Integer.parseInt(req.getParameter("productId"));
            productService.deleteProduct(productId);
            resp.sendRedirect(req.getContextPath() + "/products?msg=deleted");
            return;
        }

        try {
            int productId = 0;
            String idStr = req.getParameter("productId");
            if (idStr != null && !idStr.isEmpty()) productId = Integer.parseInt(idStr);

            int customerId;
            if (currentUser instanceof CustomerUser cu) {
                customerId = cu.getCustomerId();
            } else {
                customerId = Integer.parseInt(req.getParameter("customerId"));
            }

            Product product = new Product();
            product.setProductId(productId);
            product.setCustomerId(customerId);
            product.setProductName(req.getParameter("productName"));
            product.setProductCode(req.getParameter("productCode"));
            product.setDescription(req.getParameter("description"));
            product.setUnit(req.getParameter("unit"));
            product.setStatus(req.getParameter("status") != null ? req.getParameter("status") : "ACTIVE");

            productService.saveProduct(product);
            resp.sendRedirect(req.getContextPath() + "/products?msg=saved");
        } catch (Exception e) {
            resp.sendRedirect(req.getContextPath() + "/products?error=" + java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
        }
    }
}
