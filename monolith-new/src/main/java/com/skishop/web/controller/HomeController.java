package com.skishop.web.controller;

import com.skishop.service.catalog.ProductService;
import com.skishop.domain.product.Product;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@Controller
public class HomeController {

    private final ProductService productService;

    public HomeController(ProductService productService) {
        this.productService = productService;
    }

    @RequestMapping(value = {"/", "/home"}, method = {RequestMethod.GET, RequestMethod.POST})
    public String home(Model model) {
        List<Product> featured = productService.search(null, null, null, 0, 6);
        model.addAttribute("featuredProducts", featured);
        return "home";
    }
}
