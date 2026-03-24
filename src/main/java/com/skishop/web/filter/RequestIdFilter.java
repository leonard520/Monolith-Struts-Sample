package com.skishop.web.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class RequestIdFilter implements Filter {
    private static final String HEADER_NAME = "X-Request-Id";
    private static final String MDC_KEY = "reqId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String requestId = null;
        if (request instanceof HttpServletRequest) {
            requestId = ((HttpServletRequest) request).getHeader(HEADER_NAME);
        }
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        MDC.put(MDC_KEY, requestId);
        if (response instanceof HttpServletResponse) {
            ((HttpServletResponse) response).setHeader(HEADER_NAME, requestId);
        }
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}
