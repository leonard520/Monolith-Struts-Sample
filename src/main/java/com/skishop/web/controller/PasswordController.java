package com.skishop.web.controller;

import com.skishop.common.util.PasswordHasher;
import com.skishop.dao.user.PasswordResetTokenDao;
import com.skishop.domain.user.PasswordResetToken;
import com.skishop.domain.user.User;
import com.skishop.service.mail.MailService;
import com.skishop.service.user.UserService;
import com.skishop.web.form.PasswordResetForm;
import com.skishop.web.form.PasswordResetRequestForm;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PasswordController {
    private final UserService userService;
    private final PasswordResetTokenDao tokenDao;
    private final MailService mailService;

    public PasswordController(UserService userService, PasswordResetTokenDao tokenDao, MailService mailService) {
        this.userService = userService;
        this.tokenDao = tokenDao;
        this.mailService = mailService;
    }

    @GetMapping("/password/forgot")
    public String showForgot() {
        return "auth/password/forgot";
    }

    @PostMapping("/password/forgot")
    public String forgot(@ModelAttribute PasswordResetRequestForm form) {
        User user = userService.findByEmail(form.getEmail());
        if (user != null) {
            PasswordResetToken token = new PasswordResetToken();
            token.setId(UUID.randomUUID().toString());
            token.setUserId(user.getId());
            token.setToken(UUID.randomUUID().toString());
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.HOUR_OF_DAY, 1);
            token.setExpiresAt(cal.getTime());
            token.setUsedAt(null);
            tokenDao.insert(token);
            mailService.enqueuePasswordReset(user.getEmail(), token.getToken());
        }
        return "auth/password/forgot_complete";
    }

    @GetMapping("/password/reset")
    public String showReset(@RequestParam(required = false) String token, Model model) {
        if (token == null || token.isEmpty()) {
            model.addAttribute("errorMessage", "トークンが無効または期限切れです");
            model.addAttribute("tokenError", true);
            model.addAttribute("token", "");
            return "auth/password/reset";
        }
        PasswordResetToken prt = tokenDao.findByToken(token);
        if (prt == null || prt.getUsedAt() != null || (prt.getExpiresAt() != null && prt.getExpiresAt().before(new Date()))) {
            model.addAttribute("errorMessage", "トークンが無効または期限切れです");
            model.addAttribute("tokenError", true);
        }
        model.addAttribute("token", token);
        return "auth/password/reset";
    }

    @PostMapping("/password/reset")
    public String reset(@ModelAttribute PasswordResetForm form, Model model) {
        PasswordResetToken prt = tokenDao.findByToken(form.getToken());
        if (prt == null || prt.getUsedAt() != null || (prt.getExpiresAt() != null && prt.getExpiresAt().before(new Date()))) {
            model.addAttribute("errorMessage", "トークンが無効または期限切れです");
            return "auth/password/reset";
        }
        if (!form.getPassword().equals(form.getPasswordConfirm())) {
            model.addAttribute("errorMessage", "パスワードが一致しません");
            model.addAttribute("token", form.getToken());
            return "auth/password/reset";
        }
        User user = userService.findById(prt.getUserId());
        if (user == null) {
            model.addAttribute("errorMessage", "ユーザーが見つかりません");
            return "auth/password/reset";
        }
        String salt = PasswordHasher.generateSalt();
        String hashed = PasswordHasher.hash(form.getPassword(), salt);
        userService.updatePassword(user.getId(), hashed, salt);
        tokenDao.markUsed(prt.getId());
        return "redirect:/login";
    }
}
