package com.skishop.web.controller;

import com.skishop.domain.cart.CartItem;
import com.skishop.domain.order.Order;
import com.skishop.domain.user.User;
import com.skishop.service.cart.CartService;
import com.skishop.service.order.OrderFacade;
import com.skishop.web.form.CheckoutForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class CheckoutController {

    private static final Logger log = LoggerFactory.getLogger(CheckoutController.class);

    private final OrderFacade orderFacade;
    private final CartService cartService;

    public CheckoutController(OrderFacade orderFacade, CartService cartService) {
        this.orderFacade = orderFacade;
        this.cartService = cartService;
    }

    @GetMapping("/checkout")
    public String showCheckout(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        String cartId = session != null ? (String) session.getAttribute("cartId") : null;
        model.addAttribute("checkoutForm", new CheckoutForm());
        if (cartId != null) {
            populateCartModel(cartId, model);
        } else {
            model.addAttribute("cartItems", new ArrayList<>());
            model.addAttribute("cartSubtotal", BigDecimal.ZERO);
        }
        return "cart/checkout";
    }

    @PostMapping("/checkout")
    public String placeOrder(@ModelAttribute CheckoutForm form,
                             HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        String cartId = form.getCartId();
        if ((cartId == null || cartId.isEmpty()) && session != null) {
            cartId = (String) session.getAttribute("cartId");
            form.setCartId(cartId);
        }
        User user = session != null ? (User) session.getAttribute("loginUser") : null;
        String userId = user != null ? user.getId() : null;

        try {
            Order order = orderFacade.placeOrder(cartId, form.getCouponCode(),
                    form.getUsePoints(), form.toPaymentInfo(), userId);
            model.addAttribute("order", order);
            return "cart/confirmation";
        } catch (RuntimeException e) {
            log.warn("Checkout failed: {}", e.getMessage());
            List<String> errors = new ArrayList<>();
            errors.add("Checkout failed. Please try again.");
            model.addAttribute("errors", errors);
            if (cartId != null) {
                populateCartModel(cartId, model);
            } else {
                model.addAttribute("cartItems", new ArrayList<>());
                model.addAttribute("cartSubtotal", BigDecimal.ZERO);
            }
            model.addAttribute("checkoutForm", form);
            return "cart/checkout";
        }
    }

    private void populateCartModel(String cartId, Model model) {
        List<CartItem> items = cartService.getItems(cartId);
        BigDecimal subtotal = cartService.calculateSubtotal(items);
        model.addAttribute("cartItems", items);
        model.addAttribute("cartSubtotal", subtotal);
        model.addAttribute("cartId", cartId);
    }
}
