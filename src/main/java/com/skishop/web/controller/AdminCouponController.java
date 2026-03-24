package com.skishop.web.controller;

import com.skishop.dao.coupon.CouponDao;
import com.skishop.domain.coupon.Coupon;
import com.skishop.web.form.admin.AdminCouponForm;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller @RequestMapping("/admin")
public class AdminCouponController {
    private final CouponDao couponDao;
    public AdminCouponController(CouponDao couponDao) { this.couponDao = couponDao; }

    @GetMapping("/coupons")
    public String list(Model model) { model.addAttribute("coupons", couponDao.listAll()); return "admin/coupons/list"; }

    @GetMapping("/coupon/edit")
    public String editForm(@RequestParam(required=false) String code, Model model) {
        AdminCouponForm form = new AdminCouponForm();
        if (code != null && !code.isEmpty()) {
            Coupon c = couponDao.findByCode(code);
            if (c != null) {
                form.setId(c.getId()); form.setCampaignId(c.getCampaignId()); form.setCode(c.getCode());
                form.setCouponType(c.getCouponType()); form.setDiscountValue(c.getDiscountValue() != null ? c.getDiscountValue().toString() : "");
                form.setMinimumAmount(c.getMinimumAmount() != null ? c.getMinimumAmount().toString() : "");
                form.setMaximumDiscount(c.getMaximumDiscount() != null ? c.getMaximumDiscount().toString() : "");
                form.setUsageLimit(c.getUsageLimit()); form.setActive(c.isActive());
                if (c.getExpiresAt() != null) form.setExpiresAt(new SimpleDateFormat("yyyy-MM-dd").format(c.getExpiresAt()));
            }
        } else { form.setId(UUID.randomUUID().toString()); form.setActive(true); }
        model.addAttribute("adminCouponForm", form);
        return "admin/coupons/edit";
    }

    @PostMapping("/coupon/edit")
    public String save(@ModelAttribute AdminCouponForm form, Model model) {
        if (form.getCode() == null || form.getCode().isEmpty()) {
            model.addAttribute("errorMessage","コードが必要です"); model.addAttribute("adminCouponForm",form); return "admin/coupons/edit"; }
        BigDecimal discountValue;
        try { discountValue = new BigDecimal(form.getDiscountValue()); }
        catch (NumberFormatException e) { model.addAttribute("errorMessage","割引値が不正です"); model.addAttribute("adminCouponForm",form); return "admin/coupons/edit"; }
        BigDecimal minAmt = parseBigDecimalOrNull(form.getMinimumAmount());
        BigDecimal maxDisc = parseBigDecimalOrNull(form.getMaximumDiscount());
        Date expiresAt = null;
        if (form.getExpiresAt() != null && !form.getExpiresAt().isEmpty()) {
            try { expiresAt = new SimpleDateFormat("yyyy-MM-dd").parse(form.getExpiresAt()); }
            catch (Exception e) { model.addAttribute("errorMessage","有効期限が不正です"); model.addAttribute("adminCouponForm",form); return "admin/coupons/edit"; }
        }
        Coupon coupon = new Coupon();
        coupon.setId(form.getId()); coupon.setCampaignId(form.getCampaignId()); coupon.setCode(form.getCode());
        coupon.setCouponType(form.getCouponType()); coupon.setDiscountValue(discountValue);
        coupon.setMinimumAmount(minAmt); coupon.setMaximumDiscount(maxDisc);
        coupon.setUsageLimit(form.getUsageLimit()); coupon.setActive(form.isActive()); coupon.setExpiresAt(expiresAt);
        couponDao.saveOrUpdate(coupon);
        return "redirect:/admin/coupons";
    }

    private BigDecimal parseBigDecimalOrNull(String s) {
        if (s == null || s.isEmpty()) return null;
        try { return new BigDecimal(s); } catch (NumberFormatException e) { return null; }
    }
}
