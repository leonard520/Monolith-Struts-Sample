package com.skishop.web.controller;

import com.skishop.domain.order.Order;
import com.skishop.service.order.OrderFacade;
import com.skishop.service.order.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminOrderController {
    private final OrderService orderService;
    private final OrderFacade orderFacade;

    public AdminOrderController(OrderService orderService, OrderFacade orderFacade) {
        this.orderService = orderService;
        this.orderFacade = orderFacade;
    }

    @GetMapping("/orders")
    public String list(Model model) {
        model.addAttribute("orders", orderService.listAll(200));
        return "admin/orders/list";
    }

    @GetMapping("/orders/detail")
    public String detail(@RequestParam(required = false) String id, Model model) {
        if (id != null && !id.isEmpty()) {
            Order order = orderService.findById(id);
            if (order != null) {
                model.addAttribute("order", order);
                model.addAttribute("orderItems", orderService.listItems(id));
            }
        }
        return "admin/orders/detail";
    }

    @PostMapping("/order/update")
    public String update(@RequestParam(required = false) String id,
                         @RequestParam(required = false) String status,
                         @RequestParam(required = false) String paymentStatus,
                         RedirectAttributes ra) {
        if (id == null || id.isEmpty()) { ra.addFlashAttribute("errorMessage","注文IDが必要です"); return "redirect:/admin/orders"; }
        Order order = orderService.findById(id);
        if (order == null) { ra.addFlashAttribute("errorMessage","注文が見つかりません"); return "redirect:/admin/orders"; }
        if (status != null && !status.isEmpty()) orderService.updateStatus(id, status);
        if (paymentStatus != null && !paymentStatus.isEmpty()) orderService.updatePaymentStatus(id, paymentStatus);
        return "redirect:/admin/orders";
    }

    @PostMapping("/order/refund")
    public String refund(@RequestParam(required = false) String id, RedirectAttributes ra) {
        if (id == null || id.isEmpty()) { ra.addFlashAttribute("errorMessage","注文IDが必要です"); return "redirect:/admin/orders"; }
        try {
            orderFacade.returnOrder(id, null);
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMessage", "返金に失敗しました");
        }
        return "redirect:/admin/orders";
    }
}
