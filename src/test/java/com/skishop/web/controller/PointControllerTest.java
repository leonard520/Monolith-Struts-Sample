package com.skishop.web.controller;

import com.skishop.domain.point.PointAccount;
import com.skishop.domain.user.User;
import com.skishop.service.point.PointService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PointController.class)
class PointControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private PointService pointService;

    @Test
    void points_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/points"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));
    }

    @Test
    void points_authenticated_returns200WithBalance() throws Exception {
        User user = new User();
        user.setId("u-1");
        user.setRole("USER");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginUser", user);

        PointAccount account = new PointAccount();
        account.setBalance(500);
        when(pointService.getAccount("u-1")).thenReturn(account);

        mockMvc.perform(get("/points").session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("points/balance"))
            .andExpect(model().attributeExists("pointBalance"));
    }
}
