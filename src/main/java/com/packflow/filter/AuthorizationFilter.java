package com.packflow.filter;

import com.packflow.model.Role;
import com.packflow.model.User;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Enforces Role-Based Access Control (RBAC) across endpoints.
 * Admin, Manager, and Customer access boundaries are strictly guarded.
 */
@WebFilter(filterName = "AuthorizationFilter", urlPatterns = "/*")
public class AuthorizationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = req.getRequestURI().substring(req.getContextPath().length());

        // Ignore public and static paths
        if (path.equals("/login") || path.equals("/logout") || path.equals("/unauthorized") ||
            path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/images/") ||
            path.endsWith(".jsp")) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

        if (currentUser == null) {
            chain.doFilter(request, response);
            return;
        }

        Role role = currentUser.getRole();

        // Admin-only endpoints
        if (path.startsWith("/users")) {
            if (role != Role.ADMIN) {
                res.sendRedirect(req.getContextPath() + "/unauthorized");
                return;
            }
        }

        // Manager / Admin operational endpoints (Customers must NOT access)
        if (path.startsWith("/inventory") ||
            path.startsWith("/packaging-services") ||
            path.startsWith("/tasks") ||
            path.startsWith("/quality-checks") ||
            path.startsWith("/dispatches") ||
            path.startsWith("/reports")) {

            if (role == Role.CUSTOMER) {
                res.sendRedirect(req.getContextPath() + "/unauthorized");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
