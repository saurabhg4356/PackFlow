package com.packflow.filter;

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
 * Intercepts requests to ensure user is authenticated before accessing protected pages.
 */
@WebFilter(filterName = "AuthenticationFilter", urlPatterns = "/*")
public class AuthenticationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = req.getRequestURI().substring(req.getContextPath().length());

        // Whitelisted public resources
        boolean isPublicResource = path.equals("/login") ||
                                   path.equals("/logout") ||
                                   path.equals("/unauthorized") ||
                                   path.startsWith("/css/") ||
                                   path.startsWith("/js/") ||
                                   path.startsWith("/images/") ||
                                   path.equals("/404.jsp") ||
                                   path.equals("/500.jsp") ||
                                   path.equals("/unauthorized.jsp");

        if (isPublicResource) {
            chain.doFilter(request, response);
            return;
        }

        // Verify session
        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

        if (currentUser == null) {
            // Prevent browser caching of sensitive pages
            res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            res.setHeader("Pragma", "no-cache");
            res.setDateHeader("Expires", 0);

            // Redirect to login
            res.sendRedirect(req.getContextPath() + "/login?error=session_expired");
            return;
        }

        // User is authenticated
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
