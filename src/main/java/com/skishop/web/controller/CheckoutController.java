package com.skishop.web.controller;

import com.skishop.domain.order.Order;
import com.skishop.domain.user.User;
import com.skishop.service.order.OrderFacade;
import com.skishop.web.form.CheckoutForm;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class CheckoutController {
    private final OrderFacade orderFacade;

    public CheckoutController(OrderFacade orderFacade) {
        this.orderFacade = orderFacade;
    }

    @GetMapping("/checkout")
    public String showCheckout() {
        return "cart/checkout";
    }

    @PostMapping("/checkout")
    public String checkout(@ModelAttribute CheckoutForm form, HttpSession session, Model model) {
        String cartId = form.getCartId();
        if (cartId == null || cartId.isEmpty()) {
            cartId = (String) session.getAttribute("cartId");
        }
        User user = (User) session.getAttribute("loginUser");
        String userId = user != null ? user.getId() : null;
        try {
            Order order = orderFacade.placeOrder(cartId, form.getCouponCode(), form.getUsePoints(),
                    form.toPaymentInfo(), userId);
            model.addAttribute("order", order);
            return "cart/confirmation";
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", "チェックアウトに失敗しました");
            return "cart/checkout";
        }
    }
}
