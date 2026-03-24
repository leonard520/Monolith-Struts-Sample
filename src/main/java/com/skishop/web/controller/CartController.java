package com.skishop.web.controller;

import com.skishop.domain.cart.Cart;
import com.skishop.domain.cart.CartItem;
import com.skishop.domain.user.User;
import com.skishop.service.cart.CartService;
import com.skishop.web.form.AddCartForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class CartController {
    private static final int CART_COOKIE_MAX_AGE = 30 * 24 * 60 * 60;
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/cart")
    public String viewCart(HttpSession session, HttpServletRequest request, HttpServletResponse response, Model model) {
        String cartId = getOrCreateCart(session, request, response);
        populateCartModel(cartId, model);
        return "cart/view";
    }

    @PostMapping("/cart")
    public String addToCart(@ModelAttribute AddCartForm form, HttpSession session,
                            HttpServletRequest request, HttpServletResponse response) {
        String cartId = getOrCreateCart(session, request, response);
        if (form.getProductId() != null && !form.getProductId().isEmpty()) {
            cartService.addItem(cartId, form.getProductId(), form.getQuantity());
        }
        return "redirect:/cart";
    }

    private String getOrCreateCart(HttpSession session, HttpServletRequest request, HttpServletResponse response) {
        String cartId = (String) session.getAttribute("cartId");
        if (cartId == null) {
            User user = (User) session.getAttribute("loginUser");
            Cart cart = cartService.createCart(user != null ? user.getId() : null, session.getId());
            cartId = cart.getId();
            session.setAttribute("cartId", cartId);
            addCartCookie(request, response, cartId);
        }
        return cartId;
    }

    private void populateCartModel(String cartId, Model model) {
        List<CartItem> items = cartService.getItems(cartId);
        BigDecimal subtotal = cartService.calculateSubtotal(items);
        model.addAttribute("cartItems", items);
        model.addAttribute("cartSubtotal", subtotal);
        model.addAttribute("cartId", cartId);
    }

    private void addCartCookie(HttpServletRequest request, HttpServletResponse response, String cartId) {
        String path = request.getContextPath();
        if (path == null || path.isEmpty()) path = "/";
        StringBuilder header = new StringBuilder();
        header.append("CART_ID=").append(cartId);
        header.append("; Max-Age=").append(CART_COOKIE_MAX_AGE);
        header.append("; Path=").append(path);
        if (request.isSecure()) header.append("; Secure");
        header.append("; HttpOnly");
        response.addHeader("Set-Cookie", header.toString());
    }
}
