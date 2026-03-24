package com.skishop.web.controller;

import com.skishop.service.order.OrderFacade;
import com.skishop.service.order.OrderService;
import com.skishop.domain.user.User;
import com.skishop.domain.order.Order;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private OrderFacade orderFacade;
    @MockBean private OrderService orderService;

    @Test
    void orderHistory() throws Exception {
        User user = new User();
        user.setId("u-1");
        user.setRole("USER");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginUser", user);
        when(orderService.listByUserId("u-1")).thenReturn(List.of());

        mockMvc.perform(get("/orders").session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("orders/history"));
    }
}
