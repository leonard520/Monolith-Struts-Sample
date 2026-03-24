package com.skishop.web.controller;

import com.skishop.dao.shipping.ShippingMethodDao;
import com.skishop.domain.shipping.ShippingMethod;
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

@WebMvcTest(AdminShippingController.class)
class AdminShippingControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private ShippingMethodDao shippingMethodDao;

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
    void listShipping_asAdmin_returns200() throws Exception {
        when(shippingMethodDao.listAll()).thenReturn(List.of());
        mockMvc.perform(get("/admin/shipping").session(adminSession()))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/shipping/list"))
            .andExpect(model().attributeExists("shippingMethods"));
    }

    @Test
    void listShipping_asNonAdmin_forbidden() throws Exception {
        User user = new User();
        user.setId("u-1");
        user.setRole("USER");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginUser", user);
        mockMvc.perform(get("/admin/shipping").session(session))
            .andExpect(status().isForbidden());
    }

    @Test
    void editForm_noCode_returnsEmptyForm() throws Exception {
        mockMvc.perform(get("/admin/shipping/edit").session(adminSession()))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/shipping/edit"))
            .andExpect(model().attributeExists("adminShippingMethodForm"));
    }

    @Test
    void editForm_withCode_returnsPopulatedForm() throws Exception {
        ShippingMethod method = new ShippingMethod();
        method.setId("s-1");
        method.setCode("STANDARD");
        method.setName("Standard Shipping");
        method.setFee(BigDecimal.valueOf(500));
        method.setActive(true);
        method.setSortOrder(1);
        when(shippingMethodDao.findByCode("STANDARD")).thenReturn(method);

        mockMvc.perform(get("/admin/shipping/edit")
                .param("code", "STANDARD")
                .session(adminSession()))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/shipping/edit"))
            .andExpect(model().attributeExists("adminShippingMethodForm"));
    }

    @Test
    void saveShipping_validData_redirects() throws Exception {
        when(shippingMethodDao.findByCode("EXPRESS")).thenReturn(null);

        mockMvc.perform(post("/admin/shipping/edit")
                .session(adminSession())
                .param("id", "s-new")
                .param("code", "EXPRESS")
                .param("name", "Express Shipping")
                .param("fee", "1000")
                .param("_csrfToken", "token"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/shipping"));
    }

    @Test
    void saveShipping_missingCode_returnsError() throws Exception {
        mockMvc.perform(post("/admin/shipping/edit")
                .session(adminSession())
                .param("id", "s-new")
                .param("name", "Test")
                .param("fee", "1000")
                .param("_csrfToken", "token"))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/shipping/edit"))
            .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    void saveShipping_invalidFee_returnsError() throws Exception {
        mockMvc.perform(post("/admin/shipping/edit")
                .session(adminSession())
                .param("id", "s-new")
                .param("code", "EXPRESS")
                .param("name", "Express")
                .param("fee", "not-a-number")
                .param("_csrfToken", "token"))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/shipping/edit"))
            .andExpect(model().attributeExists("errorMessage"));
    }
}
