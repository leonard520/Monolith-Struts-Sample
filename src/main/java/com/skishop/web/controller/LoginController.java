package com.skishop.web.controller;

import com.skishop.service.auth.AuthResult;
import com.skishop.service.auth.AuthService;
import com.skishop.web.form.LoginForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class LoginController {
    private final AuthService authService;

    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String showLogin() {
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute LoginForm form, HttpServletRequest request, HttpSession session, Model model) {
        AuthResult result = authService.authenticate(form.getEmail(), form.getPassword(),
                request.getRemoteAddr(), request.getHeader("User-Agent"));
        if (!result.isSuccess()) {
            model.addAttribute("errorMessage", "ログインに失敗しました");
            return "auth/login";
        }
        session.invalidate();
        HttpSession newSession = request.getSession(true);
        newSession.setAttribute("loginUser", result.getUser());
        return "redirect:/home";
    }
}
