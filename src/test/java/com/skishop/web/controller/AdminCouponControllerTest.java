package com.skishop.web.controller;

import com.skishop.dao.coupon.CouponDao;
import com.skishop.domain.coupon.Coupon;
import com.skishop.domain.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminCouponController.class)
class AdminCouponControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private CouponDao couponDao;

    private MockHttpSession adminSession() {
        User admin = new User();
        admin.setId("admin-1");
        admin.setRole("ADMIN");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginUser", admin);
        session.setAttribute("_csrfToken", "token");
        return session;
    }

    @Test
    void listCoupons_asAdmin_returns200() throws Exception {
        when(couponDao.listAll()).thenReturn(List.of());
        mockMvc.perform(get("/admin/coupons").session(adminSession()))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/coupons/list"))
            .andExpect(model().attributeExists("coupons"));
    }

    @Test
    void listCoupons_asNonAdmin_forbidden() throws Exception {
        User user = new User();
        user.setId("u-1");
        user.setRole("USER");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginUser", user);
        mockMvc.perform(get("/admin/coupons").session(session))
            .andExpect(status().isForbidden());
    }

    @Test
    void editForm_noCode_returnsEmptyForm() throws Exception {
        mockMvc.perform(get("/admin/coupon/edit").session(adminSession()))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/coupons/edit"))
            .andExpect(model().attributeExists("adminCouponForm"));
    }

    @Test
    void editForm_withCode_returnsPopulatedForm() throws Exception {
        Coupon coupon = new Coupon();
        coupon.setId("c-1");
        coupon.setCode("SAVE10");
        coupon.setDiscountValue(BigDecimal.valueOf(10));
        coupon.setActive(true);
        when(couponDao.findByCode("SAVE10")).thenReturn(coupon);

        mockMvc.perform(get("/admin/coupon/edit")
                .param("code", "SAVE10")
                .session(adminSession()))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/coupons/edit"))
            .andExpect(model().attributeExists("adminCouponForm"));
    }

    @Test
    void saveCoupon_validData_redirects() throws Exception {
        when(couponDao.findByCode("NEWCODE")).thenReturn(null);

        mockMvc.perform(post("/admin/coupon/edit")
                .session(adminSession())
                .param("id", "c-new")
                .param("code", "NEWCODE")
                .param("discountValue", "500")
                .param("_csrfToken", "token"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/coupons"));
    }

    @Test
    void saveCoupon_missingCode_returnsError() throws Exception {
        mockMvc.perform(post("/admin/coupon/edit")
                .session(adminSession())
                .param("id", "c-new")
                .param("discountValue", "500")
                .param("_csrfToken", "token"))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/coupons/edit"))
            .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    void saveCoupon_invalidDiscountValue_returnsError() throws Exception {
        mockMvc.perform(post("/admin/coupon/edit")
                .session(adminSession())
                .param("id", "c-new")
                .param("code", "NEWCODE")
                .param("discountValue", "not-a-number")
                .param("_csrfToken", "token"))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/coupons/edit"))
            .andExpect(model().attributeExists("errorMessage"));
    }
}
