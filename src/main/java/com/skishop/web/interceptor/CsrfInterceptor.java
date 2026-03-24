package com.skishop.web.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class CsrfInterceptor implements HandlerInterceptor {
    private static final String CSRF_TOKEN_KEY = "_csrfToken";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            HttpSession session = request.getSession(true);
            if (session.getAttribute(CSRF_TOKEN_KEY) == null) {
                session.setAttribute(CSRF_TOKEN_KEY, UUID.randomUUID().toString());
            }
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF token missing");
            return false;
        }

        String sessionToken = (String) session.getAttribute(CSRF_TOKEN_KEY);
        String requestToken = request.getParameter(CSRF_TOKEN_KEY);

        if (sessionToken == null || !sessionToken.equals(requestToken)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF token invalid");
            return false;
        }

        // Regenerate token for redirect safety (avoids double-submit)
        session.setAttribute(CSRF_TOKEN_KEY, UUID.randomUUID().toString());
        return true;
    }
}
