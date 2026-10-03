package com.packflow.controller;

import com.packflow.model.PackagingServiceItem;
import com.packflow.model.Role;
import com.packflow.model.User;
import com.packflow.service.PackagingServiceService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Controller managing packaging service options and baseline pricing.
 */
@WebServlet(name = "PackagingServiceServlet", urlPatterns = {"/packaging-services"})
public class PackagingServiceServlet extends HttpServlet {

    private PackagingServiceService service;

    @Override
    public void init() {
        this.service = new PackagingServiceService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (User) session.getAttribute("currentUser");

        if (currentUser.getRole() == Role.CUSTOMER) {
            resp.sendRedirect(req.getContextPath() + "/unauthorized");
            return;
        }

        List<PackagingServiceItem> services = service.getAllServices();
        req.setAttribute("services", services);

        String msg = req.getParameter("msg");
        if ("saved".equals(msg)) req.setAttribute("successMessage", "Packaging service saved successfully.");
        else if ("deleted".equals(msg)) req.setAttribute("successMessage", "Packaging service deleted.");

        req.getRequestDispatcher("/packaging-services.jsp").forward(req, resp);
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
            int serviceId = Integer.parseInt(req.getParameter("serviceId"));
            service.deleteService(serviceId);
            resp.sendRedirect(req.getContextPath() + "/packaging-services?msg=deleted");
            return;
        }

        try {
            int serviceId = 0;
            String idStr = req.getParameter("serviceId");
            if (idStr != null && !idStr.isEmpty()) serviceId = Integer.parseInt(idStr);

            PackagingServiceItem item = new PackagingServiceItem();
            item.setServiceId(serviceId);
            item.setServiceName(req.getParameter("serviceName"));
            item.setDescription(req.getParameter("description"));
            item.setBasePrice(new BigDecimal(req.getParameter("basePrice")));
            item.setStatus(req.getParameter("status") != null ? req.getParameter("status") : "ACTIVE");

            service.saveService(item);
            resp.sendRedirect(req.getContextPath() + "/packaging-services?msg=saved");
        } catch (Exception e) {
            resp.sendRedirect(req.getContextPath() + "/packaging-services?error=" + java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
        }
    }
}
