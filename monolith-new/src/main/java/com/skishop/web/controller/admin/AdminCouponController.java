package com.skishop.web.controller.admin;

import com.skishop.dao.coupon.CouponDao;
import com.skishop.domain.coupon.Coupon;
import com.skishop.web.form.admin.AdminCouponForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminCouponController {

    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private final CouponDao couponDao;

    public AdminCouponController(CouponDao couponDao) {
        this.couponDao = couponDao;
    }

    @GetMapping("/coupons")
    public String list(Model model) {
        List<Coupon> coupons = couponDao.listAll();
        model.addAttribute("coupons", coupons);
        return "admin/coupons/list";
    }

    @GetMapping("/coupon/edit")
    public String showEdit(@RequestParam(value = "code", required = false) String code,
                           Model model) {
        AdminCouponForm couponForm = new AdminCouponForm();
        if (code != null && code.length() > 0) {
            Coupon coupon = couponDao.findByCode(code);
            if (coupon != null) {
                couponForm.setId(coupon.getId());
                couponForm.setCampaignId(coupon.getCampaignId());
                couponForm.setCode(coupon.getCode());
                couponForm.setCouponType(coupon.getCouponType());
                couponForm.setDiscountValue(toStringValue(coupon.getDiscountValue()));
                couponForm.setDiscountType(coupon.getDiscountType());
                couponForm.setMinimumAmount(toStringValue(coupon.getMinimumAmount()));
                couponForm.setMaximumDiscount(toStringValue(coupon.getMaximumDiscount()));
                couponForm.setUsageLimit(coupon.getUsageLimit());
                couponForm.setActive(coupon.isActive());
                couponForm.setExpiresAt(formatDate(coupon.getExpiresAt()));
            }
        } else {
            couponForm.setId(UUID.randomUUID().toString());
            couponForm.setActive(true);
        }
        model.addAttribute("couponForm", couponForm);
        return "admin/coupons/edit";
    }

    @PostMapping("/coupon/edit")
    public String save(@ModelAttribute("couponForm") AdminCouponForm couponForm,
                       Model model) {
        if (couponForm.getCode() == null || couponForm.getCode().length() == 0) {
            model.addAttribute("error", "Coupon code is required");
            model.addAttribute("couponForm", couponForm);
            return "admin/coupons/edit";
        }

        BigDecimal discountValue;
        try {
            discountValue = new BigDecimal(couponForm.getDiscountValue());
        } catch (NumberFormatException e) {
            model.addAttribute("error", "Invalid discount value");
            model.addAttribute("couponForm", couponForm);
            return "admin/coupons/edit";
        }

        BigDecimal minimumAmount;
        try {
            minimumAmount = parseOptionalDecimal(couponForm.getMinimumAmount());
        } catch (NumberFormatException e) {
            model.addAttribute("error", "Invalid minimum amount");
            model.addAttribute("couponForm", couponForm);
            return "admin/coupons/edit";
        }

        BigDecimal maximumDiscount;
        try {
            maximumDiscount = parseOptionalDecimal(couponForm.getMaximumDiscount());
        } catch (NumberFormatException e) {
            model.addAttribute("error", "Invalid maximum discount");
            model.addAttribute("couponForm", couponForm);
            return "admin/coupons/edit";
        }

        Date expiresAt;
        try {
            expiresAt = parseOptionalDate(couponForm.getExpiresAt());
        } catch (ParseException e) {
            model.addAttribute("error", "Invalid date format (expected yyyy-MM-dd)");
            model.addAttribute("couponForm", couponForm);
            return "admin/coupons/edit";
        }

        Coupon coupon = new Coupon();
        coupon.setId(couponForm.getId());
        if (coupon.getId() == null || coupon.getId().length() == 0) {
            coupon.setId(UUID.randomUUID().toString());
        }
        coupon.setCampaignId(couponForm.getCampaignId());
        coupon.setCode(couponForm.getCode());
        coupon.setCouponType(couponForm.getCouponType());
        coupon.setDiscountValue(discountValue);
        coupon.setDiscountType(couponForm.getDiscountType());
        coupon.setMinimumAmount(minimumAmount);
        coupon.setMaximumDiscount(maximumDiscount);
        coupon.setUsageLimit(couponForm.getUsageLimit());
        coupon.setActive(couponForm.isActive());
        coupon.setExpiresAt(expiresAt);
        couponDao.saveOrUpdate(coupon);

        return "redirect:/admin/coupons";
    }

    private BigDecimal parseOptionalDecimal(String value) {
        if (value == null || value.trim().length() == 0) {
            return null;
        }
        return new BigDecimal(value.trim());
    }

    private Date parseOptionalDate(String value) throws ParseException {
        if (value == null || value.trim().length() == 0) {
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
        formatter.setLenient(false);
        return formatter.parse(value.trim());
    }

    private String formatDate(Date date) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
        return formatter.format(date);
    }

    private String toStringValue(BigDecimal value) {
        return value != null ? value.toString() : null;
    }
}
