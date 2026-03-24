package com.skishop.web.controller;

import com.skishop.dao.inventory.InventoryDao;
import com.skishop.dao.product.PriceDao;
import com.skishop.dao.product.ProductDao;
import com.skishop.domain.inventory.Inventory;
import com.skishop.domain.product.Price;
import com.skishop.domain.product.Product;
import com.skishop.service.catalog.ProductService;
import com.skishop.web.form.admin.AdminProductForm;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminProductController {
    private final ProductService productService;
    private final ProductDao productDao;
    private final PriceDao priceDao;
    private final InventoryDao inventoryDao;

    public AdminProductController(ProductService productService, ProductDao productDao, PriceDao priceDao, InventoryDao inventoryDao) {
        this.productService = productService;
        this.productDao = productDao;
        this.priceDao = priceDao;
        this.inventoryDao = inventoryDao;
    }

    @GetMapping("/products")
    public String list(Model model) {
        model.addAttribute("products", productService.search(null, null, 0, 200));
        return "admin/products/list";
    }

    @GetMapping("/product/edit")
    public String editForm(@RequestParam(required = false) String id, Model model) {
        AdminProductForm form = new AdminProductForm();
        if (id != null && !id.isEmpty()) {
            Product p = productService.findById(id);
            if (p != null) {
                form.setId(p.getId()); form.setName(p.getName()); form.setBrand(p.getBrand());
                form.setDescription(p.getDescription()); form.setCategoryId(p.getCategoryId()); form.setStatus(p.getStatus());
                Price price = priceDao.findByProductId(id);
                if (price != null && price.getRegularPrice() != null) form.setPrice(price.getRegularPrice().toString());
                Inventory inv = inventoryDao.findByProductId(id);
                if (inv != null) form.setInventoryQty(inv.getQuantity());
            }
        } else {
            form.setId("P" + UUID.randomUUID().toString().replace("-","").substring(0,12));
            form.setStatus("ACTIVE"); form.setPrice("0"); form.setInventoryQty(0);
        }
        model.addAttribute("adminProductForm", form);
        return "admin/products/edit";
    }

    @PostMapping("/product/edit")
    public String save(@ModelAttribute AdminProductForm form, Model model, RedirectAttributes ra) {
        if (form.getId() == null || form.getId().isEmpty()) {
            model.addAttribute("errorMessage", "商品IDが必要です");
            model.addAttribute("adminProductForm", form);
            return "admin/products/edit";
        }
        BigDecimal priceValue;
        try { priceValue = new BigDecimal(form.getPrice()); }
        catch (NumberFormatException e) {
            model.addAttribute("errorMessage", "価格が不正です");
            model.addAttribute("adminProductForm", form);
            return "admin/products/edit";
        }
        Product existing = productService.findById(form.getId());
        Product product = new Product();
        product.setId(form.getId()); product.setName(form.getName()); product.setBrand(form.getBrand());
        product.setDescription(form.getDescription()); product.setCategoryId(form.getCategoryId()); product.setStatus(form.getStatus());
        if (existing == null) {
            product.setSku("SKU-" + form.getId());
            Date now = new Date(); product.setCreatedAt(now); product.setUpdatedAt(now);
            productDao.insert(product);
        } else {
            product.setSku(existing.getSku());
            productDao.update(product);
        }
        Price price = new Price();
        price.setId(UUID.randomUUID().toString()); price.setProductId(form.getId());
        price.setRegularPrice(priceValue); price.setCurrencyCode("JPY");
        priceDao.saveOrUpdate(price);
        Inventory inv = inventoryDao.findByProductId(form.getId());
        int qty = form.getInventoryQty();
        String status = qty > 0 ? "AVAILABLE" : "OUT_OF_STOCK";
        if (inv == null) {
            inv = new Inventory(); inv.setId(UUID.randomUUID().toString()); inv.setProductId(form.getId());
            inv.setQuantity(qty); inv.setReservedQuantity(0); inv.setStatus(status);
            inventoryDao.insert(inv);
        } else {
            inventoryDao.updateQuantity(form.getId(), qty, status);
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/product/delete")
    public String delete(@RequestParam(required = false) String id, RedirectAttributes ra) {
        if (id == null || id.isEmpty()) { ra.addFlashAttribute("errorMessage","商品IDが必要です"); return "redirect:/admin/products"; }
        Product p = productService.findById(id);
        if (p == null) { ra.addFlashAttribute("errorMessage","商品が見つかりません"); return "redirect:/admin/products"; }
        productService.deactivateProduct(id);
        return "redirect:/admin/products";
    }
}
