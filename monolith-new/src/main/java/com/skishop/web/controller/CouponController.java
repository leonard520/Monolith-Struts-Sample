package com.skishop.web.controller;

import com.skishop.domain.cart.Cart;
import com.skishop.domain.cart.CartItem;
import com.skishop.domain.coupon.Coupon;
import com.skishop.domain.user.User;
import com.skishop.service.cart.CartService;
import com.skishop.service.coupon.CouponService;
import com.skishop.web.form.CouponForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class CouponController {

    private final CouponService couponService;
    private final CartService cartService;

    public CouponController(CouponService couponService, CartService cartService) {
        this.couponService = couponService;
        this.cartService = cartService;
    }

    @GetMapping("/coupons")
    public String available(Model model) {
        model.addAttribute("coupons", couponService.listActiveCoupons());
        return "coupons/available";
    }

    @PostMapping("/coupons/apply")
    public String apply(@ModelAttribute CouponForm form,
                        HttpServletRequest request,
                        HttpServletResponse response,
                        Model model) {
        HttpSession session = request.getSession(false);
        String cartId = session != null ? (String) session.getAttribute("cartId") : null;

        if (cartId == null) {
            // No cart — ensure one exists, then show cart view with error
            session = request.getSession(true);
            User user = (User) session.getAttribute("loginUser");
            Cart cart = cartService.createCart(user != null ? user.getId() : null, session.getId());
            cartId = cart.getId();
            session.setAttribute("cartId", cartId);
            model.addAttribute("cartItems", List.of());
            model.addAttribute("cartSubtotal", BigDecimal.ZERO);
            model.addAttribute("cartId", cartId);
            model.addAttribute("errorMessage", "カートが見つかりません。");
            return "cart/view";
        }

        List<CartItem> items = cartService.getItems(cartId);
        BigDecimal subtotal = cartService.calculateSubtotal(items);
        model.addAttribute("cartItems", items);
        model.addAttribute("cartSubtotal", subtotal);
        model.addAttribute("cartId", cartId);

        try {
            Coupon coupon = couponService.validateCoupon(form.getCode(), subtotal);
            BigDecimal discount = couponService.calculateDiscount(coupon, subtotal);
            model.addAttribute("coupon", coupon);
            model.addAttribute("discountAmount", discount);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", "クーポンコードが無効です。");
        }
        return "cart/view";
    }
}
