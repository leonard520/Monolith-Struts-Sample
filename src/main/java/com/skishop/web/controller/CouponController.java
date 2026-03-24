package com.skishop.web.controller;

import com.skishop.domain.cart.CartItem;
import com.skishop.domain.coupon.Coupon;
import com.skishop.service.cart.CartService;
import com.skishop.service.coupon.CouponService;
import com.skishop.web.form.CouponForm;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class CouponController {
    private final CouponService couponService;
    private final CartService cartService;

    public CouponController(CouponService couponService, CartService cartService) {
        this.couponService = couponService;
        this.cartService = cartService;
    }

    @GetMapping("/coupons")
    public String listCoupons(Model model) {
        model.addAttribute("coupons", couponService.listActiveCoupons());
        return "coupons/available";
    }

    @PostMapping("/coupons/apply")
    public String applyCoupon(@ModelAttribute CouponForm form, HttpSession session, Model model) {
        String cartId = session != null ? (String) session.getAttribute("cartId") : null;
        if (cartId == null) {
            model.addAttribute("errorMessage", "カートが見つかりません");
            return "redirect:/cart";
        }
        List<CartItem> items = cartService.getItems(cartId);
        BigDecimal subtotal = cartService.calculateSubtotal(items);
        model.addAttribute("cartItems", items);
        model.addAttribute("cartSubtotal", subtotal);
        try {
            Coupon coupon = couponService.validateCoupon(form.getCode(), subtotal);
            BigDecimal discount = couponService.calculateDiscount(coupon, subtotal);
            model.addAttribute("coupon", coupon);
            model.addAttribute("discountAmount", discount);
            return "cart/view";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", "クーポンが無効です");
            return "cart/view";
        }
    }
}
