package com.skishop.web.controller;

import com.skishop.domain.order.Order;
import com.skishop.domain.order.OrderItem;
import com.skishop.domain.user.User;
import com.skishop.service.order.OrderFacade;
import com.skishop.service.order.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminOrderController.class)
class AdminOrderControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private OrderService orderService;
    @MockBean private OrderFacade orderFacade;

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
    void listOrders_asAdmin_returns200() throws Exception {
        when(orderService.listAll(200)).thenReturn(List.of());
        mockMvc.perform(get("/admin/orders").session(adminSession()))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/orders/list"))
            .andExpect(model().attributeExists("orders"));
    }

    @Test
    void listOrders_asNonAdmin_forbidden() throws Exception {
        User user = new User();
        user.setId("u-1");
        user.setRole("USER");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginUser", user);
        mockMvc.perform(get("/admin/orders").session(session))
            .andExpect(status().isForbidden());
    }

    @Test
    void orderDetail_withId_returns200() throws Exception {
        Order order = new Order();
        order.setId("ORD-1");
        when(orderService.findById("ORD-1")).thenReturn(order);
        when(orderService.listItems("ORD-1")).thenReturn(List.of());

        mockMvc.perform(get("/admin/orders/detail")
                .param("id", "ORD-1")
                .session(adminSession()))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/orders/detail"))
            .andExpect(model().attributeExists("order"));
    }

    @Test
    void orderDetail_noId_returns200WithNoOrder() throws Exception {
        mockMvc.perform(get("/admin/orders/detail").session(adminSession()))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/orders/detail"));
    }

    @Test
    void updateOrder_validIdAndStatus_redirects() throws Exception {
        Order order = new Order();
        order.setId("ORD-1");
        when(orderService.findById("ORD-1")).thenReturn(order);

        mockMvc.perform(post("/admin/order/update")
                .session(adminSession())
                .param("id", "ORD-1")
                .param("status", "SHIPPED")
                .param("_csrfToken", "token"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/orders"));

        verify(orderService).updateStatus("ORD-1", "SHIPPED");
    }

    @Test
    void updateOrder_missingId_redirectsWithError() throws Exception {
        mockMvc.perform(post("/admin/order/update")
                .session(adminSession())
                .param("_csrfToken", "token"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/orders"));
    }

    @Test
    void refundOrder_success_redirects() throws Exception {
        mockMvc.perform(post("/admin/order/refund")
                .session(adminSession())
                .param("id", "ORD-1")
                .param("_csrfToken", "token"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/orders"));

        verify(orderFacade).returnOrder("ORD-1", null);
    }

    @Test
    void refundOrder_runtimeException_redirectsWithError() throws Exception {
        doThrow(new RuntimeException("refund failed")).when(orderFacade).returnOrder("ORD-1", null);

        mockMvc.perform(post("/admin/order/refund")
                .session(adminSession())
                .param("id", "ORD-1")
                .param("_csrfToken", "token"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/orders"))
            .andExpect(flash().attributeExists("errorMessage"));
    }
}
