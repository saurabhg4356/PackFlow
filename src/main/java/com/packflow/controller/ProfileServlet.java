package com.packflow.controller;

import com.packflow.model.User;
import com.packflow.service.UserService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Controller allowing logged in users to view their account info and change their password.
 */
@WebServlet(name = "ProfileServlet", urlPatterns = {"/profile"})
public class ProfileServlet extends HttpServlet {

    private UserService userService;

    @Override
    public void init() {
        this.userService = new UserService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (User) session.getAttribute("currentUser");

        req.setAttribute("user", currentUser);

        String msg = req.getParameter("msg");
        if ("pwd_changed".equals(msg)) req.setAttribute("successMessage", "Password updated successfully.");

        String error = req.getParameter("error");
        if (error != null) req.setAttribute("errorMessage", error);

        req.getRequestDispatcher("/profile.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (User) session.getAttribute("currentUser");

        String oldPwd = req.getParameter("oldPassword");
        String newPwd = req.getParameter("newPassword");
        String confirmPwd = req.getParameter("confirmPassword");

        if (newPwd == null || !newPwd.equals(confirmPwd)) {
            resp.sendRedirect(req.getContextPath() + "/profile?error=" +
                    java.net.URLEncoder.encode("New passwords do not match", "UTF-8"));
            return;
        }

        try {
            userService.changePassword(currentUser.getUserId(), oldPwd, newPwd);
            resp.sendRedirect(req.getContextPath() + "/profile?msg=pwd_changed");
        } catch (Exception e) {
            resp.sendRedirect(req.getContextPath() + "/profile?error=" +
                    java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
        }
    }
}
