package com.skishop.web.controller;

import com.skishop.dao.user.PasswordResetTokenDao;
import com.skishop.service.mail.MailService;
import com.skishop.service.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PasswordController.class)
class PasswordControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private UserService userService;
    @MockBean private PasswordResetTokenDao passwordResetTokenDao;
    @MockBean private MailService mailService;

    @Test
    void showForgotForm() throws Exception {
        mockMvc.perform(get("/password/forgot"))
            .andExpect(status().isOk())
            .andExpect(view().name("auth/password/forgot"));
    }

    @Test
    void showResetForm() throws Exception {
        mockMvc.perform(get("/password/reset").param("token", "test-token"))
            .andExpect(status().isOk())
            .andExpect(view().name("auth/password/reset"));
    }
}
