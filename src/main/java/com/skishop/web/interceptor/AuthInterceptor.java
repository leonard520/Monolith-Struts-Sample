package com.skishop.web.interceptor;

import com.skishop.domain.user.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private static final String LOGIN_USER_KEY = "loginUser";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty()) {
            path = path.substring(contextPath.length());
        }

        String requiredRole = getRequiredRole(path);
        if (requiredRole == null) {
            return true; // public path
        }

        HttpSession session = request.getSession(false);
        User user = session != null ? (User) session.getAttribute(LOGIN_USER_KEY) : null;

        if (user == null) {
            response.sendRedirect(contextPath + "/login");
            return false;
        }

        if ("ADMIN".equals(requiredRole) && !"ADMIN".equalsIgnoreCase(user.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        return true;
    }

    private String getRequiredRole(String path) {
        if (path.startsWith("/admin/") || path.equals("/admin")) {
            return "ADMIN";
        }
        if (path.startsWith("/cart") || path.startsWith("/checkout")
                || path.startsWith("/orders") || path.startsWith("/points")
                || path.equals("/coupons/apply") || path.startsWith("/account/")) {
            return "USER";
        }
        return null; // public
    }
}
