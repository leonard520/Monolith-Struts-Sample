package com.skishop.web.controller;

import com.skishop.dao.inventory.InventoryDao;
import com.skishop.dao.product.PriceDao;
import com.skishop.dao.product.ProductDao;
import com.skishop.domain.inventory.Inventory;
import com.skishop.domain.product.Price;
import com.skishop.domain.product.Product;
import com.skishop.domain.user.User;
import com.skishop.service.catalog.ProductService;
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

@WebMvcTest(AdminProductController.class)
class AdminProductControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private ProductService productService;
    @MockBean private ProductDao productDao;
    @MockBean private PriceDao priceDao;
    @MockBean private InventoryDao inventoryDao;

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
    void listProducts_asAdmin_returns200() throws Exception {
        when(productService.search(any(), any(), anyInt(), anyInt())).thenReturn(List.of());
        mockMvc.perform(get("/admin/products").session(adminSession()))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/products/list"))
            .andExpect(model().attributeExists("products"));
    }

    @Test
    void listProducts_asNonAdmin_forbidden() throws Exception {
        User user = new User();
        user.setId("u-1");
        user.setRole("USER");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginUser", user);
        mockMvc.perform(get("/admin/products").session(session))
            .andExpect(status().isForbidden());
    }

    @Test
    void listProducts_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/admin/products"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));
    }

    @Test
    void editForm_noId_returnsEmptyForm() throws Exception {
        mockMvc.perform(get("/admin/product/edit").session(adminSession()))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/products/edit"))
            .andExpect(model().attributeExists("adminProductForm"));
    }

    @Test
    void editForm_withId_returnsPopulatedForm() throws Exception {
        Product p = new Product();
        p.setId("PSK001");
        p.setName("Test Product");
        p.setBrand("TestBrand");
        p.setStatus("ACTIVE");
        when(productService.findById("PSK001")).thenReturn(p);

        Price price = new Price();
        price.setRegularPrice(BigDecimal.valueOf(10000));
        when(priceDao.findByProductId("PSK001")).thenReturn(price);

        Inventory inv = new Inventory();
        inv.setQuantity(50);
        when(inventoryDao.findByProductId("PSK001")).thenReturn(inv);

        mockMvc.perform(get("/admin/product/edit").param("id", "PSK001").session(adminSession()))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/products/edit"))
            .andExpect(model().attributeExists("adminProductForm"));
    }

    @Test
    void saveProduct_validData_redirects() throws Exception {
        when(productService.findById(anyString())).thenReturn(null);

        mockMvc.perform(post("/admin/product/edit")
                .session(adminSession())
                .param("id", "P123")
                .param("name", "New Product")
                .param("price", "5000")
                .param("status", "ACTIVE")
                .param("inventoryQty", "10")
                .param("_csrfToken", "token"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/products"));
    }

    @Test
    void saveProduct_invalidPrice_returnsError() throws Exception {
        mockMvc.perform(post("/admin/product/edit")
                .session(adminSession())
                .param("id", "P123")
                .param("name", "New Product")
                .param("price", "not-a-number")
                .param("_csrfToken", "token"))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/products/edit"))
            .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    void deleteProduct_validId_redirects() throws Exception {
        Product p = new Product();
        p.setId("PSK001");
        when(productService.findById("PSK001")).thenReturn(p);

        mockMvc.perform(post("/admin/product/delete")
                .session(adminSession())
                .param("id", "PSK001")
                .param("_csrfToken", "token"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/products"));
    }

    @Test
    void deleteProduct_missingId_redirectsWithError() throws Exception {
        mockMvc.perform(post("/admin/product/delete")
                .session(adminSession())
                .param("_csrfToken", "token"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/products"));
    }
}
