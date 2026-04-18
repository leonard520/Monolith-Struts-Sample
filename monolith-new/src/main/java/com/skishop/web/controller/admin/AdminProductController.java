package com.skishop.web.controller.admin;

import com.skishop.dao.inventory.InventoryDao;
import com.skishop.dao.product.PriceDao;
import com.skishop.dao.product.ProductDao;
import com.skishop.domain.inventory.Inventory;
import com.skishop.domain.product.Price;
import com.skishop.domain.product.Product;
import com.skishop.service.catalog.ProductService;
import com.skishop.web.form.admin.AdminProductForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminProductController {

    private static final int MAX_ADMIN_PRODUCTS = 200;
    private static final int PRODUCT_ID_RANDOM_LENGTH = 12;

    private final ProductService productService;
    private final ProductDao productDao;
    private final PriceDao priceDao;
    private final InventoryDao inventoryDao;

    public AdminProductController(ProductService productService,
                                  ProductDao productDao,
                                  PriceDao priceDao,
                                  InventoryDao inventoryDao) {
        this.productService = productService;
        this.productDao = productDao;
        this.priceDao = priceDao;
        this.inventoryDao = inventoryDao;
    }

    @GetMapping("/products")
    public String list(Model model) {
        List<Product> products = productService.search(null, null, 0, MAX_ADMIN_PRODUCTS);
        model.addAttribute("products", products);
        return "admin/products/list";
    }

    @GetMapping("/product/edit")
    public String showEdit(@RequestParam(value = "id", required = false) String productId,
                           Model model) {
        AdminProductForm productForm = new AdminProductForm();
        if (productId != null && productId.length() > 0) {
            Product product = productService.findById(productId);
            if (product != null) {
                productForm.setId(product.getId());
                productForm.setName(product.getName());
                productForm.setBrand(product.getBrand());
                productForm.setDescription(product.getDescription());
                productForm.setCategoryId(product.getCategoryId());
                productForm.setStatus(product.getStatus());
                Price price = priceDao.findByProductId(productId);
                if (price != null && price.getRegularPrice() != null) {
                    productForm.setPrice(price.getRegularPrice().toString());
                }
                Inventory inventory = inventoryDao.findByProductId(productId);
                if (inventory != null) {
                    productForm.setInventoryQty(inventory.getQuantity());
                }
            }
        } else {
            productForm.setId(generateProductId());
            productForm.setStatus("ACTIVE");
            productForm.setPrice("0");
            productForm.setInventoryQty(0);
        }
        model.addAttribute("productForm", productForm);
        return "admin/products/edit";
    }

    @PostMapping("/product/edit")
    public String save(@ModelAttribute("productForm") AdminProductForm productForm,
                       Model model) {
        if (productForm.getId() == null || productForm.getId().length() == 0) {
            model.addAttribute("error", "Product ID is required");
            model.addAttribute("productForm", productForm);
            return "admin/products/edit";
        }

        BigDecimal priceValue;
        try {
            priceValue = new BigDecimal(productForm.getPrice());
        } catch (NumberFormatException e) {
            model.addAttribute("error", "Invalid price value");
            model.addAttribute("productForm", productForm);
            return "admin/products/edit";
        }

        Product existing = productService.findById(productForm.getId());

        Product product = new Product();
        product.setId(productForm.getId());
        product.setName(productForm.getName());
        product.setBrand(productForm.getBrand());
        product.setDescription(productForm.getDescription());
        product.setCategoryId(productForm.getCategoryId());
        product.setStatus(productForm.getStatus());
        if (existing == null) {
            product.setSku("SKU-" + productForm.getId());
            Date now = new Date();
            product.setCreatedAt(now);
            product.setUpdatedAt(now);
            productDao.insert(product);
        } else {
            product.setSku(existing.getSku());
            productDao.update(product);
        }

        Price price = new Price();
        price.setId(UUID.randomUUID().toString());
        price.setProductId(productForm.getId());
        price.setRegularPrice(priceValue);
        price.setSalePrice(null);
        price.setCurrencyCode("JPY");
        price.setSaleStartDate(null);
        price.setSaleEndDate(null);
        priceDao.saveOrUpdate(price);

        Inventory inventory = inventoryDao.findByProductId(productForm.getId());
        int quantity = productForm.getInventoryQty();
        String status = quantity > 0 ? "AVAILABLE" : "OUT_OF_STOCK";
        if (inventory == null) {
            inventory = new Inventory();
            inventory.setId(UUID.randomUUID().toString());
            inventory.setProductId(productForm.getId());
            inventory.setQuantity(quantity);
            inventory.setReservedQuantity(0);
            inventory.setStatus(status);
            inventoryDao.insert(inventory);
        } else {
            inventoryDao.updateQuantity(productForm.getId(), quantity, status);
        }

        return "redirect:/admin/products";
    }

    @PostMapping("/product/delete")
    public String delete(@RequestParam("id") String productId, Model model) {
        if (productId == null || productId.length() == 0) {
            return "redirect:/admin/products";
        }
        Product product = productService.findById(productId);
        if (product == null) {
            return "redirect:/admin/products";
        }
        productService.deactivateProduct(product.getId());
        return "redirect:/admin/products";
    }

    private String generateProductId() {
        String raw = UUID.randomUUID().toString().replace("-", "");
        return "P" + raw.substring(0, PRODUCT_ID_RANDOM_LENGTH);
    }
}
