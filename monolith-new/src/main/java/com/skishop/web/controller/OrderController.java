package com.skishop.web.controller;

import com.skishop.domain.order.Order;
import com.skishop.domain.order.OrderItem;
import com.skishop.domain.user.User;
import com.skishop.service.order.OrderFacade;
import com.skishop.service.order.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;
    private final OrderFacade orderFacade;

    public OrderController(OrderService orderService, OrderFacade orderFacade) {
        this.orderService = orderService;
        this.orderFacade = orderFacade;
    }

    @GetMapping("/orders")
    public String history(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        User user = session != null ? (User) session.getAttribute("loginUser") : null;
        if (user == null) {
            return "redirect:/login";
        }
        List<Order> orders;
        if (user.getRole() != null && "ADMIN".equalsIgnoreCase(user.getRole())) {
            orders = orderService.listAll(50);
        } else {
            orders = orderService.listByUserId(user.getId());
        }
        model.addAttribute("orders", orders);
        return "orders/history";
    }

    @GetMapping("/orders/detail")
    public String detail(@RequestParam(value = "id", required = false) String orderId,
                         HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        User user = session != null ? (User) session.getAttribute("loginUser") : null;
        if (user == null) {
            return "redirect:/login";
        }
        if (orderId != null && !orderId.trim().isEmpty()) {
            Order order = orderService.findById(orderId);
            if (order != null && user.getId().equals(order.getUserId())) {
                List<OrderItem> items = orderService.listItems(orderId);
                model.addAttribute("order", order);
                model.addAttribute("orderItems", items);
            }
        }
        return "orders/detail";
    }

    @PostMapping("/orders/{id}/cancel")
    public String cancel(@PathVariable("id") String orderId,
                         HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        User user = session != null ? (User) session.getAttribute("loginUser") : null;
        if (orderId == null || orderId.isEmpty()) {
            model.addAttribute("errors", List.of("Order not found."));
            return "redirect:/orders";
        }
        try {
            orderFacade.cancelOrder(orderId, user != null ? user.getId() : null);
        } catch (RuntimeException e) {
            log.warn("Cancel failed for order {}: {}", orderId, e.getMessage());
        }
        return "redirect:/orders";
    }

    @PostMapping("/orders/{id}/return")
    public String returnOrder(@PathVariable("id") String orderId,
                              HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        User user = session != null ? (User) session.getAttribute("loginUser") : null;
        if (orderId == null || orderId.isEmpty()) {
            model.addAttribute("errors", List.of("Order not found."));
            return "redirect:/orders";
        }
        try {
            orderFacade.returnOrder(orderId, user != null ? user.getId() : null);
        } catch (RuntimeException e) {
            log.warn("Return failed for order {}: {}", orderId, e.getMessage());
        }
        return "redirect:/orders";
    }
}
