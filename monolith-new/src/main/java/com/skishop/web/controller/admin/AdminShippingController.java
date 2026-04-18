package com.skishop.web.controller.admin;

import com.skishop.dao.shipping.ShippingMethodDao;
import com.skishop.domain.shipping.ShippingMethod;
import com.skishop.web.form.admin.AdminShippingMethodForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminShippingController {

    private final ShippingMethodDao shippingMethodDao;

    public AdminShippingController(ShippingMethodDao shippingMethodDao) {
        this.shippingMethodDao = shippingMethodDao;
    }

    @GetMapping("/shipping")
    public String list(Model model) {
        List<ShippingMethod> methods = shippingMethodDao.listAll();
        model.addAttribute("shippingMethods", methods);
        return "admin/shipping/list";
    }

    @GetMapping("/shipping/edit")
    public String showEdit(@RequestParam(value = "code", required = false) String code,
                           Model model) {
        AdminShippingMethodForm shippingForm = new AdminShippingMethodForm();
        if (code != null && code.length() > 0) {
            ShippingMethod method = shippingMethodDao.findByCode(code);
            if (method != null) {
                shippingForm.setId(method.getId());
                shippingForm.setCode(method.getCode());
                shippingForm.setName(method.getName());
                shippingForm.setFee(method.getFee() != null ? method.getFee().toString() : null);
                shippingForm.setActive(method.isActive());
                shippingForm.setSortOrder(method.getSortOrder());
            }
        } else {
            shippingForm.setId(UUID.randomUUID().toString());
            shippingForm.setActive(true);
        }
        model.addAttribute("shippingForm", shippingForm);
        return "admin/shipping/edit";
    }

    @PostMapping("/shipping/edit")
    public String save(@ModelAttribute("shippingForm") AdminShippingMethodForm shippingForm,
                       Model model) {
        if (shippingForm.getCode() == null || shippingForm.getCode().length() == 0) {
            model.addAttribute("error", "Shipping code is required");
            model.addAttribute("shippingForm", shippingForm);
            return "admin/shipping/edit";
        }

        BigDecimal fee;
        try {
            fee = new BigDecimal(shippingForm.getFee());
        } catch (NumberFormatException e) {
            model.addAttribute("error", "Invalid fee value");
            model.addAttribute("shippingForm", shippingForm);
            return "admin/shipping/edit";
        }

        ShippingMethod existing = shippingMethodDao.findByCode(shippingForm.getCode());
        ShippingMethod method = new ShippingMethod();
        String methodId = existing != null ? existing.getId() : shippingForm.getId();
        if (methodId == null || methodId.length() == 0) {
            methodId = UUID.randomUUID().toString();
        }
        method.setId(methodId);
        method.setCode(shippingForm.getCode());
        method.setName(shippingForm.getName());
        method.setFee(fee);
        method.setActive(shippingForm.isActive());
        method.setSortOrder(shippingForm.getSortOrder());

        if (existing == null) {
            shippingMethodDao.insert(method);
        } else {
            shippingMethodDao.update(method);
        }

        return "redirect:/admin/shipping";
    }
}
