package com.skishop.web.controller;

import com.skishop.domain.product.Category;
import com.skishop.domain.product.Product;
import com.skishop.service.catalog.CategoryService;
import com.skishop.service.catalog.ProductService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ProductController {
    private static final int DEFAULT_SIZE = 10;
    private final ProductService productService;
    private final CategoryService categoryService;

    public ProductController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping("/products")
    public String list(@RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String categoryId,
                       @RequestParam(required = false) String sort,
                       Model model) {
        if (page <= 0) page = 1;
        if (size <= 0) size = DEFAULT_SIZE;
        int offset = (page - 1) * size;
        List<Product> products = productService.search(keyword, categoryId, sort, offset, size);
        List<Category> categories = categoryService.listAll();
        List<String[]> categoryOptions = new ArrayList<>();
        categoryOptions.add(new String[]{"指定なし", ""});
        if (categories != null) {
            for (Category c : categories) {
                categoryOptions.add(new String[]{c.getName(), c.getId()});
            }
        }
        model.addAttribute("productList", products);
        model.addAttribute("categoryOptions", categoryOptions);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        return "products/list";
    }

    @GetMapping("/product")
    public String detail(@RequestParam(required = false) String id, Model model) {
        if (id == null || id.isEmpty()) return "products/notfound";
        Product product = productService.findById(id);
        if (product == null) return "products/notfound";
        model.addAttribute("product", product);
        return "products/detail";
    }
}
