package com.skishop.web.controller;

import com.skishop.domain.product.Category;
import com.skishop.domain.product.Product;
import com.skishop.service.catalog.CategoryService;
import com.skishop.service.catalog.ProductService;
import com.skishop.web.form.ProductSearchForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

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
    public String list(@ModelAttribute ProductSearchForm searchForm, Model model) {
        int page = searchForm.getPage();
        int size = searchForm.getSize();
        if (page <= 0) {
            page = 1;
        }
        if (size <= 0) {
            size = DEFAULT_SIZE;
        }
        String keyword = searchForm.getKeyword();
        String categoryId = searchForm.getCategoryId();
        String sort = searchForm.getSort();

        int offset = (page - 1) * size;
        List<Product> products = productService.search(keyword, categoryId, sort, offset, size);

        List<Category> categories = categoryService.listAll();
        List<LabelValue> categoryOptions = new ArrayList<>();
        categoryOptions.add(new LabelValue("指定なし", ""));
        if (categories != null) {
            for (Category c : categories) {
                categoryOptions.add(new LabelValue(c.getName(), c.getId()));
            }
        }

        model.addAttribute("productList", products);
        model.addAttribute("categoryOptions", categoryOptions);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        return "products/list";
    }

    @GetMapping("/product")
    public String detail(@RequestParam(name = "id", required = false) String productId, Model model) {
        if (productId == null || productId.isEmpty()) {
            return "products/notfound";
        }
        Product product = productService.findById(productId);
        if (product == null) {
            return "products/notfound";
        }
        model.addAttribute("product", product);
        return "products/detail";
    }

    /**
     * Simple label-value pair replacing Struts LabelValueBean.
     */
    public static class LabelValue {
        private final String label;
        private final String value;

        public LabelValue(String label, String value) {
            this.label = label;
            this.value = value;
        }

        public String getLabel() {
            return label;
        }

        public String getValue() {
            return value;
        }
    }
}
