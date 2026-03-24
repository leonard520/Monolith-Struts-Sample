package com.skishop.web.controller;

import com.skishop.domain.user.User;
import com.skishop.service.point.PointService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PointController {
    private final PointService pointService;

    public PointController(PointService pointService) {
        this.pointService = pointService;
    }

    @GetMapping("/points")
    public String balance(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loginUser");
        model.addAttribute("pointBalance", pointService.getAccount(user.getId()));
        return "points/balance";
    }
}
