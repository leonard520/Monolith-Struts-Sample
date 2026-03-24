package com.skishop.web.interceptor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

class CsrfInterceptorTest {

    private CsrfInterceptor interceptor;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        interceptor = new CsrfInterceptor();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void getRequest_generatesTokenWhenMissing() throws Exception {
        request.setMethod("GET");
        boolean result = interceptor.preHandle(request, response, new Object());
        assertTrue(result);
        assertNotNull(request.getSession().getAttribute("_csrfToken"));
    }

    @Test
    void getRequest_preservesExistingToken() throws Exception {
        request.setMethod("GET");
        request.getSession(true).setAttribute("_csrfToken", "existing-token");
        boolean result = interceptor.preHandle(request, response, new Object());
        assertTrue(result);
        assertEquals("existing-token", request.getSession().getAttribute("_csrfToken"));
    }

    @Test
    void postWithValidToken_allowedAndTokenReset() throws Exception {
        request.setMethod("POST");
        request.getSession(true).setAttribute("_csrfToken", "valid-token");
        request.setParameter("_csrfToken", "valid-token");

        boolean result = interceptor.preHandle(request, response, new Object());
        assertTrue(result);
        // Token is regenerated (not removed) after successful validation
        String newToken = (String) request.getSession().getAttribute("_csrfToken");
        assertNotNull(newToken);
        assertNotEquals("valid-token", newToken);
    }

    @Test
    void postWithInvalidToken_forbidden() throws Exception {
        request.setMethod("POST");
        request.getSession(true).setAttribute("_csrfToken", "valid-token");
        request.setParameter("_csrfToken", "wrong-token");

        boolean result = interceptor.preHandle(request, response, new Object());
        assertFalse(result);
        assertEquals(403, response.getStatus());
    }

    @Test
    void postWithNoTokenParameter_forbidden() throws Exception {
        request.setMethod("POST");
        request.getSession(true).setAttribute("_csrfToken", "valid-token");

        boolean result = interceptor.preHandle(request, response, new Object());
        assertFalse(result);
        assertEquals(403, response.getStatus());
    }

    @Test
    void postWithNoSession_forbidden() throws Exception {
        request.setMethod("POST");
        boolean result = interceptor.preHandle(request, response, new Object());
        assertFalse(result);
        assertEquals(403, response.getStatus());
    }
}
