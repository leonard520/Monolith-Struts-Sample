package com.skishop.web.controller;

import com.skishop.domain.cart.CartItem;
import com.skishop.domain.coupon.Coupon;
import com.skishop.domain.user.User;
import com.skishop.service.cart.CartService;
import com.skishop.service.coupon.CouponService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CouponController.class)
class CouponControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private CouponService couponService;
    @MockBean private CartService cartService;

    @Test
    void listCoupons_returns200() throws Exception {
        when(couponService.listActiveCoupons()).thenReturn(List.of());
        mockMvc.perform(get("/coupons"))
            .andExpect(status().isOk())
            .andExpect(view().name("coupons/available"))
            .andExpect(model().attributeExists("coupons"));
    }

    @Test
    void applyCoupon_validCoupon_success() throws Exception {
        User user = new User();
        user.setId("u-1");
        user.setRole("USER");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginUser", user);
        session.setAttribute("cartId", "cart-1");
        session.setAttribute("_csrfToken", "token");

        when(cartService.getItems("cart-1")).thenReturn(List.of());
        when(cartService.calculateSubtotal(anyList())).thenReturn(BigDecimal.valueOf(10000));
        Coupon coupon = new Coupon();
        coupon.setCode("SAVE10");
        when(couponService.validateCoupon(eq("SAVE10"), any())).thenReturn(coupon);
        when(couponService.calculateDiscount(any(), any())).thenReturn(BigDecimal.valueOf(1000));

        mockMvc.perform(post("/coupons/apply")
                .session(session)
                .param("code", "SAVE10")
                .param("_csrfToken", "token"))
            .andExpect(status().isOk())
            .andExpect(view().name("cart/view"))
            .andExpect(model().attributeExists("coupon"))
            .andExpect(model().attributeExists("discountAmount"));
    }

    @Test
    void applyCoupon_invalidCoupon_error() throws Exception {
        User user = new User();
        user.setId("u-1");
        user.setRole("USER");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginUser", user);
        session.setAttribute("cartId", "cart-1");
        session.setAttribute("_csrfToken", "token");

        when(cartService.getItems("cart-1")).thenReturn(List.of());
        when(cartService.calculateSubtotal(anyList())).thenReturn(BigDecimal.valueOf(10000));
        when(couponService.validateCoupon(eq("INVALID"), any()))
            .thenThrow(new IllegalArgumentException("Invalid coupon"));

        mockMvc.perform(post("/coupons/apply")
                .session(session)
                .param("code", "INVALID")
                .param("_csrfToken", "token"))
            .andExpect(status().isOk())
            .andExpect(view().name("cart/view"))
            .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    void applyCoupon_noCartInSession_error() throws Exception {
        User user = new User();
        user.setId("u-1");
        user.setRole("USER");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginUser", user);
        session.setAttribute("_csrfToken", "token");

        mockMvc.perform(post("/coupons/apply")
                .session(session)
                .param("code", "SAVE10")
                .param("_csrfToken", "token"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/cart"));
    }
}
