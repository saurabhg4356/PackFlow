package com.packflow.controller;

import com.packflow.model.Admin;
import com.packflow.model.CustomerUser;
import com.packflow.model.Manager;
import com.packflow.model.Role;
import com.packflow.model.User;
import com.packflow.service.UserService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * Controller strictly restricted to ADMIN users for managing user credentials, roles, and access.
 */
@WebServlet(name = "UserServlet", urlPatterns = {"/users"})
public class UserServlet extends HttpServlet {

    private UserService userService;

    @Override
    public void init() {
        this.userService = new UserService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (User) session.getAttribute("currentUser");

        if (currentUser.getRole() != Role.ADMIN) {
            resp.sendRedirect(req.getContextPath() + "/unauthorized");
            return;
        }

        List<User> users = userService.getAllUsers();
        req.setAttribute("users", users);

        String msg = req.getParameter("msg");
        if ("created".equals(msg)) req.setAttribute("successMessage", "User account created successfully.");
        else if ("updated".equals(msg)) req.setAttribute("successMessage", "User account updated.");
        else if ("deleted".equals(msg)) req.setAttribute("successMessage", "User account removed.");

        req.getRequestDispatcher("/users.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (User) session.getAttribute("currentUser");

        if (currentUser.getRole() != Role.ADMIN) {
            resp.sendRedirect(req.getContextPath() + "/unauthorized");
            return;
        }

        String action = req.getParameter("action");
        if ("delete".equalsIgnoreCase(action)) {
            int userId = Integer.parseInt(req.getParameter("userId"));
            if (userId == currentUser.getUserId()) {
                resp.sendRedirect(req.getContextPath() + "/users?error=" + java.net.URLEncoder.encode("Cannot delete own account", "UTF-8"));
                return;
            }
            userService.deleteUser(userId);
            resp.sendRedirect(req.getContextPath() + "/users?msg=deleted");
            return;
        }

        try {
            int userId = 0;
            String idStr = req.getParameter("userId");
            if (idStr != null && !idStr.isEmpty()) userId = Integer.parseInt(idStr);

            String name = req.getParameter("name");
            String email = req.getParameter("email");
            String roleStr = req.getParameter("role");
            String status = req.getParameter("status");
            String password = req.getParameter("password");

            Role role = Role.fromString(roleStr);

            if (userId > 0) {
                User user = User.create(userId, name, email, "", role, status, null, null);
                userService.updateUser(user);
                resp.sendRedirect(req.getContextPath() + "/users?msg=updated");
            } else {
                User user = User.create(0, name, email, "", role, status, null, null);
                userService.registerUser(user, password);
                resp.sendRedirect(req.getContextPath() + "/users?msg=created");
            }
        } catch (Exception e) {
            resp.sendRedirect(req.getContextPath() + "/users?error=" + java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
        }
    }
}
