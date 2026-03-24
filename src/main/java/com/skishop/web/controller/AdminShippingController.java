package com.skishop.web.controller;

import com.skishop.dao.shipping.ShippingMethodDao;
import com.skishop.domain.shipping.ShippingMethod;
import com.skishop.web.form.admin.AdminShippingMethodForm;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller @RequestMapping("/admin")
public class AdminShippingController {
    private final ShippingMethodDao shippingMethodDao;
    public AdminShippingController(ShippingMethodDao shippingMethodDao) { this.shippingMethodDao = shippingMethodDao; }

    @GetMapping("/shipping")
    public String list(Model model) { model.addAttribute("shippingMethods", shippingMethodDao.listAll()); return "admin/shipping/list"; }

    @GetMapping("/shipping/edit")
    public String editForm(@RequestParam(required=false) String code, Model model) {
        AdminShippingMethodForm form = new AdminShippingMethodForm();
        if (code != null && !code.isEmpty()) {
            ShippingMethod m = shippingMethodDao.findByCode(code);
            if (m != null) {
                form.setId(m.getId()); form.setCode(m.getCode()); form.setName(m.getName());
                form.setFee(m.getFee() != null ? m.getFee().toString() : ""); form.setActive(m.isActive()); form.setSortOrder(m.getSortOrder());
            }
        } else { form.setId(UUID.randomUUID().toString()); form.setActive(true); }
        model.addAttribute("adminShippingMethodForm", form);
        return "admin/shipping/edit";
    }

    @PostMapping("/shipping/edit")
    public String save(@ModelAttribute AdminShippingMethodForm form, Model model) {
        if (form.getCode() == null || form.getCode().isEmpty()) {
            model.addAttribute("errorMessage","コードが必要です"); model.addAttribute("adminShippingMethodForm",form); return "admin/shipping/edit"; }
        BigDecimal fee;
        try { fee = new BigDecimal(form.getFee()); }
        catch (NumberFormatException e) { model.addAttribute("errorMessage","料金が不正です"); model.addAttribute("adminShippingMethodForm",form); return "admin/shipping/edit"; }
        ShippingMethod existing = shippingMethodDao.findByCode(form.getCode());
        ShippingMethod method = new ShippingMethod();
        method.setId(existing != null ? existing.getId() : (form.getId() != null ? form.getId() : UUID.randomUUID().toString()));
        method.setCode(form.getCode()); method.setName(form.getName()); method.setFee(fee);
        method.setActive(form.isActive()); method.setSortOrder(form.getSortOrder());
        if (existing == null) shippingMethodDao.insert(method); else shippingMethodDao.update(method);
        return "redirect:/admin/shipping";
    }
}
