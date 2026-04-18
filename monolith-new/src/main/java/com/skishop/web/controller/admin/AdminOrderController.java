package com.skishop.web.controller.admin;

import com.skishop.domain.order.Order;
import com.skishop.domain.order.OrderItem;
import com.skishop.service.order.OrderFacade;
import com.skishop.service.order.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminOrderController {

    private static final int MAX_ADMIN_ORDERS = 200;

    private final OrderService orderService;
    private final OrderFacade orderFacade;

    public AdminOrderController(OrderService orderService, OrderFacade orderFacade) {
        this.orderService = orderService;
        this.orderFacade = orderFacade;
    }

    @GetMapping("/orders")
    public String list(Model model) {
        List<Order> orders = orderService.listAll(MAX_ADMIN_ORDERS);
        model.addAttribute("orders", orders);
        return "admin/orders/list";
    }

    @GetMapping("/orders/detail")
    public String detail(@RequestParam(value = "orderId", required = false) String orderId,
                         Model model) {
        if (orderId != null && orderId.trim().length() > 0) {
            Order order = orderService.findById(orderId);
            if (order != null) {
                List<OrderItem> items = orderService.listItems(orderId);
                model.addAttribute("order", order);
                model.addAttribute("orderItems", items);
            }
        }
        return "admin/orders/detail";
    }

    @PostMapping("/order/update")
    public String update(@RequestParam(value = "orderId", required = false) String orderId,
                         @RequestParam(value = "status", required = false) String status,
                         @RequestParam(value = "paymentStatus", required = false) String paymentStatus,
                         Model model) {
        if (orderId == null || orderId.length() == 0) {
            return "redirect:/admin/orders";
        }
        Order order = orderService.findById(orderId);
        if (order == null) {
            return "redirect:/admin/orders";
        }
        if (status != null && status.length() > 0) {
            orderService.updateStatus(orderId, status);
        }
        if (paymentStatus != null && paymentStatus.length() > 0) {
            orderService.updatePaymentStatus(orderId, paymentStatus);
        }
        return "redirect:/admin/orders";
    }

    @PostMapping("/order/refund")
    public String refund(@RequestParam(value = "orderId", required = false) String orderId,
                         @RequestParam(value = "id", required = false) String idParam,
                         Model model) {
        String resolvedId = orderId;
        if (resolvedId == null || resolvedId.length() == 0) {
            resolvedId = idParam;
        }
        if (resolvedId == null || resolvedId.length() == 0) {
            return "redirect:/admin/orders";
        }
        try {
            orderFacade.returnOrder(resolvedId, null);
        } catch (RuntimeException e) {
            // Refund failed — redirect back to admin orders
        }
        return "redirect:/admin/orders";
    }
}
