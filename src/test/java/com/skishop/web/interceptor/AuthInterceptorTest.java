package com.skishop.web.interceptor;

import com.skishop.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

class AuthInterceptorTest {

    private AuthInterceptor interceptor;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        interceptor = new AuthInterceptor();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void unauthenticatedProtectedPath_redirectsToLogin() throws Exception {
        request.setRequestURI("/cart");
        boolean result = interceptor.preHandle(request, response, new Object());
        assertFalse(result);
        assertEquals("/login", response.getRedirectedUrl());
    }

    @Test
    void authenticatedUserAccessingUserPath_allowed() throws Exception {
        request.setRequestURI("/cart");
        User user = new User();
        user.setId("u-1");
        user.setRole("USER");
        request.getSession(true).setAttribute("loginUser", user);

        boolean result = interceptor.preHandle(request, response, new Object());
        assertTrue(result);
    }

    @Test
    void authenticatedUserAccessingAdminPath_forbidden() throws Exception {
        request.setRequestURI("/admin/products");
        User user = new User();
        user.setId("u-1");
        user.setRole("USER");
        request.getSession(true).setAttribute("loginUser", user);

        boolean result = interceptor.preHandle(request, response, new Object());
        assertFalse(result);
        assertEquals(403, response.getStatus());
    }

    @Test
    void authenticatedAdminAccessingAdminPath_allowed() throws Exception {
        request.setRequestURI("/admin/products");
        User admin = new User();
        admin.setId("a-1");
        admin.setRole("ADMIN");
        request.getSession(true).setAttribute("loginUser", admin);

        boolean result = interceptor.preHandle(request, response, new Object());
        assertTrue(result);
    }

    @Test
    void publicPath_allowedWithoutSession() throws Exception {
        request.setRequestURI("/home");
        boolean result = interceptor.preHandle(request, response, new Object());
        assertTrue(result);
    }

    @Test
    void loginPath_allowedWithoutSession() throws Exception {
        request.setRequestURI("/login");
        boolean result = interceptor.preHandle(request, response, new Object());
        assertTrue(result);
    }

    @Test
    void productsPath_allowedWithoutSession() throws Exception {
        request.setRequestURI("/products");
        boolean result = interceptor.preHandle(request, response, new Object());
        assertTrue(result);
    }
}
