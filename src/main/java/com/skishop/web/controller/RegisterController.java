package com.skishop.web.controller;

import com.skishop.common.util.PasswordHasher;
import com.skishop.domain.user.User;
import com.skishop.service.user.UserService;
import com.skishop.web.form.RegisterForm;
import java.util.Date;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RegisterController {
    private final UserService userService;

    public RegisterController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegister() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute RegisterForm form, Model model) {
        if (form.getEmail() == null || form.getEmail().isBlank()
                || form.getUsername() == null || form.getUsername().isBlank()
                || form.getPassword() == null || form.getPassword().isBlank()
                || form.getPasswordConfirm() == null || form.getPasswordConfirm().isBlank()) {
            model.addAttribute("errorMessage", "全ての項目を入力してください");
            return "auth/register";
        }
        if (!form.getPassword().equals(form.getPasswordConfirm())) {
            model.addAttribute("errorMessage", "パスワードが一致しません");
            return "auth/register";
        }
        if (userService.findByEmail(form.getEmail()) != null) {
            model.addAttribute("errorMessage", "このメールアドレスは既に登録されています");
            return "auth/register";
        }
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setEmail(form.getEmail());
        user.setUsername(form.getUsername());
        String salt = PasswordHasher.generateSalt();
        String hashed = PasswordHasher.hash(form.getPassword(), salt);
        user.setSalt(salt);
        user.setPasswordHash(hashed);
        user.setStatus("ACTIVE");
        user.setRole("USER");
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        userService.register(user);
        model.addAttribute("registrationSuccess", true);
        return "auth/login";
    }
}
