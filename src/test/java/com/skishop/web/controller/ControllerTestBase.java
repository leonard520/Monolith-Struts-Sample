package com.skishop.web.controller;

import com.skishop.domain.user.User;
import org.springframework.mock.web.MockHttpSession;

/**
 * Shared utilities for controller tests.
 */
public abstract class ControllerTestBase {

    protected static User createUser(String id, String role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        return user;
    }

    protected static MockHttpSession authenticatedSession(String userId, String role) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginUser", createUser(userId, role));
        session.setAttribute("_csrfToken", "token");
        return session;
    }

    protected static MockHttpSession adminSession() {
        return authenticatedSession("admin-1", "ADMIN");
    }

    protected static MockHttpSession userSession() {
        return authenticatedSession("u-1", "USER");
    }
}
