package com.skishop.web.controller;

import com.skishop.service.order.OrderFacade;
import com.skishop.domain.user.User;
import com.skishop.domain.order.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@WebMvcTest(CheckoutController.class)
class CheckoutControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private OrderFacade orderFacade;

    @Test
    void checkoutSuccess() throws Exception {
        User user = new User();
        user.setId("u-1");
        user.setRole("USER");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginUser", user);
        session.setAttribute("cartId", "cart-1");
        session.setAttribute("_csrfToken", "token");

        Order order = new Order();
        order.setId("o-1");
        order.setOrderNumber("ORD-1");
        when(orderFacade.placeOrder(anyString(), any(), anyInt(), any(), anyString())).thenReturn(order);

        mockMvc.perform(post("/checkout")
                .session(session)
                .param("cartId", "cart-1")
                .param("paymentMethod", "CARD")
                .param("cardNumber", "4111111111111111")
                .param("cardExpMonth", "12")
                .param("cardExpYear", "2030")
                .param("cardCvv", "123")
                .param("billingZip", "12345")
                .param("_csrfToken", "token"))
            .andExpect(status().isOk())
            .andExpect(view().name("cart/confirmation"));
    }
}
