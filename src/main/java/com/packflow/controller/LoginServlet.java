package com.packflow.controller;

import com.packflow.exception.UserNotFoundException;
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
 * Controller handling user authentication, session creation, and role redirection.
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

    private UserService userService;

    @Override
    public void init() {
        this.userService = new UserService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("currentUser") != null) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }

        String error = req.getParameter("error");
        if ("session_expired".equals(error)) {
            req.setAttribute("errorMessage", "Your session has expired. Please sign in again.");
        } else if ("invalid_credentials".equals(error)) {
            req.setAttribute("errorMessage", "Invalid email address or password.");
        }

        String msg = req.getParameter("msg");
        if ("logged_out".equals(msg)) {
            req.setAttribute("successMessage", "You have successfully signed out.");
        }

        req.getRequestDispatcher("/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = req.getParameter("email");
        String password = req.getParameter("password");

        try {
            User user = userService.authenticate(email, password);

            // Establish fresh HTTP session
            HttpSession session = req.getSession(true);
            session.setAttribute("currentUser", user);
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("userName", user.getName());
            session.setAttribute("userRole", user.getRole().name());
            session.setAttribute("userEmail", user.getEmail());

            resp.sendRedirect(req.getContextPath() + "/dashboard");
        } catch (UserNotFoundException e) {
            req.setAttribute("errorMessage", e.getMessage());
            req.setAttribute("inputEmail", email);
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
        } catch (Exception e) {
            req.setAttribute("errorMessage", "An unexpected error occurred. Please try again.");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
        }
    }
}
