package com.skishop.web.controller;

import com.skishop.domain.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LogoutController.class)
class LogoutControllerTest {

    @Autowired private MockMvc mockMvc;

    @Test
    void logoutWithActiveSession_invalidatesAndRedirects() throws Exception {
        User user = new User();
        user.setId("u-1");
        user.setRole("USER");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginUser", user);

        mockMvc.perform(get("/logout").session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/home"));

        assertTrue(session.isInvalid());
    }

    @Test
    void logoutWithNoSession_redirectsToHome() throws Exception {
        mockMvc.perform(get("/logout"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/home"));
    }
}
