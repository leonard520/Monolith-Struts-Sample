package com.skishop.web.controller;

import com.skishop.service.cart.CartService;
import com.skishop.domain.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private CartService cartService;

    @Test
    void addToCart() throws Exception {
        User user = new User();
        user.setId("u-1");
        user.setRole("USER");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginUser", user);
        session.setAttribute("_csrfToken", "token");
        session.setAttribute("cartId", "cart-1");

        mockMvc.perform(post("/cart")
                .session(session)
                .param("productId", "P001")
                .param("quantity", "2")
                .param("_csrfToken", "token"))
            .andExpect(status().is3xxRedirection());
    }
}
