package com.skishop.web.controller;

import com.skishop.service.auth.AuthService;
import com.skishop.service.auth.AuthResult;
import com.skishop.domain.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@WebMvcTest(LoginController.class)
class LoginControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private AuthService authService;

    @Test
    void showLoginForm() throws Exception {
        mockMvc.perform(get("/login"))
            .andExpect(status().isOk())
            .andExpect(view().name("auth/login"));
    }

    @Test
    void loginSuccess() throws Exception {
        User user = new User();
        user.setId("u-1");
        user.setRole("USER");
        when(authService.authenticate(any(), any(), any(), any()))
            .thenReturn(AuthResult.success(user));

        mockMvc.perform(post("/login")
                .param("email", "user@example.com")
                .param("password", "password123")
                .sessionAttr("_csrfToken", "token")
                .param("_csrfToken", "token"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/home"));
    }

    @Test
    void loginFailure() throws Exception {
        when(authService.authenticate(any(), any(), any(), any()))
            .thenReturn(AuthResult.failure("Invalid credentials"));

        mockMvc.perform(post("/login")
                .param("email", "user@example.com")
                .param("password", "wrong")
                .sessionAttr("_csrfToken", "token")
                .param("_csrfToken", "token"))
            .andExpect(status().isOk())
            .andExpect(view().name("auth/login"));
    }
}
