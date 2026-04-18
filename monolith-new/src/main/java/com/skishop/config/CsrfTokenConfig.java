package com.skishop.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class CsrfTokenConfig implements HandlerInterceptor {

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");
        if (csrfToken == null) {
            csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        }
        if (csrfToken != null && modelAndView != null) {
            modelAndView.addObject("_csrf", csrfToken);
            modelAndView.addObject("_csrfToken", csrfToken.getToken());
        }
    }
}
