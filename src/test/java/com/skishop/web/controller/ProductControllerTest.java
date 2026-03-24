package com.skishop.web.controller;

import com.skishop.service.catalog.ProductService;
import com.skishop.service.catalog.CategoryService;
import com.skishop.domain.product.Product;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private ProductService productService;
    @MockBean private CategoryService categoryService;

    @Test
    void listProducts() throws Exception {
        when(productService.search(any(), any(), any(), anyInt(), anyInt())).thenReturn(List.of());
        when(categoryService.listAll()).thenReturn(List.of());
        mockMvc.perform(get("/products"))
            .andExpect(status().isOk())
            .andExpect(view().name("products/list"));
    }

    @Test
    void productDetail() throws Exception {
        Product p = new Product();
        p.setId("P001");
        p.setName("Ski A");
        when(productService.findById("P001")).thenReturn(p);
        mockMvc.perform(get("/product").param("id", "P001"))
            .andExpect(status().isOk())
            .andExpect(view().name("products/detail"));
    }

    @Test
    void productNotFound() throws Exception {
        when(productService.findById("NOPE")).thenReturn(null);
        mockMvc.perform(get("/product").param("id", "NOPE"))
            .andExpect(status().isOk())
            .andExpect(view().name("products/notfound"));
    }
}
