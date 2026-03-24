package com.skishop.web.controller;

import com.skishop.domain.order.Order;
import com.skishop.domain.order.OrderItem;
import com.skishop.domain.user.User;
import com.skishop.service.order.OrderFacade;
import com.skishop.service.order.OrderService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class OrderController {
    private final OrderService orderService;
    private final OrderFacade orderFacade;

    public OrderController(OrderService orderService, OrderFacade orderFacade) {
        this.orderService = orderService;
        this.orderFacade = orderFacade;
    }

    @GetMapping("/orders")
    public String list(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loginUser");
        List<Order> orders;
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            orders = orderService.listAll(50);
        } else {
            orders = orderService.listByUserId(user.getId());
        }
        model.addAttribute("orders", orders);
        return "orders/history";
    }

    @GetMapping("/orders/detail")
    public String detail(@RequestParam(required = false) String id, HttpSession session, Model model) {
        if (id == null || id.isEmpty()) return "orders/detail";
        User user = (User) session.getAttribute("loginUser");
        Order order = orderService.findById(id);
        if (order != null && (user.getId().equals(order.getUserId()) || "ADMIN".equalsIgnoreCase(user.getRole()))) {
            model.addAttribute("order", order);
            model.addAttribute("orderItems", orderService.listItems(id));
        }
        return "orders/detail";
    }

    @PostMapping("/orders/cancel")
    public String cancel(@RequestParam(required = false) String id, HttpSession session, RedirectAttributes ra) {
        if (id == null || id.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "注文IDが必要です");
            return "redirect:/orders";
        }
        User user = (User) session.getAttribute("loginUser");
        try {
            orderFacade.cancelOrder(id, user.getId());
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMessage", "キャンセルに失敗しました");
        }
        return "redirect:/orders";
    }

    @PostMapping("/orders/return")
    public String returnOrder(@RequestParam(required = false) String id, HttpSession session, RedirectAttributes ra) {
        if (id == null || id.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "注文IDが必要です");
            return "redirect:/orders";
        }
        User user = (User) session.getAttribute("loginUser");
        try {
            orderFacade.returnOrder(id, user.getId());
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMessage", "返品に失敗しました");
        }
        return "redirect:/orders";
    }
}
