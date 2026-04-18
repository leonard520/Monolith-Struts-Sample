package com.skishop.web.controller;

import com.skishop.common.util.PasswordHasher;
import com.skishop.dao.user.PasswordResetTokenDao;
import com.skishop.domain.user.PasswordResetToken;
import com.skishop.domain.user.User;
import com.skishop.service.auth.AuthResult;
import com.skishop.service.auth.AuthService;
import com.skishop.service.mail.MailService;
import com.skishop.service.user.UserService;
import com.skishop.web.form.LoginForm;
import com.skishop.web.form.PasswordResetForm;
import com.skishop.web.form.PasswordResetRequestForm;
import com.skishop.web.form.RegisterForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Controller
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final PasswordResetTokenDao passwordResetTokenDao;
    private final MailService mailService;

    public AuthController(AuthService authService, UserService userService,
                          PasswordResetTokenDao passwordResetTokenDao, MailService mailService) {
        this.authService = authService;
        this.userService = userService;
        this.passwordResetTokenDao = passwordResetTokenDao;
        this.mailService = mailService;
    }

    // ---- LOGIN ----

    @GetMapping("/login")
    public String showLogin(Model model) {
        model.addAttribute("loginForm", new LoginForm());
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginForm") LoginForm form,
                        BindingResult bindingResult,
                        HttpServletRequest request,
                        Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/login";
        }

        AuthResult result = authService.authenticate(
                form.getEmail(), form.getPassword(),
                request.getRemoteAddr(), request.getHeader("User-Agent"));

        if (!result.isSuccess()) {
            model.addAttribute("errors", List.of("メールアドレスまたはパスワードが正しくありません。"));
            return "auth/login";
        }

        // Invalidate old session (session fixation prevention)
        // Preserve CSRF token so that redirect followers (curl -L) don't get 403
        HttpSession oldSession = request.getSession(false);
        Object csrfToken = null;
        if (oldSession != null) {
            csrfToken = oldSession.getAttribute(
                    "org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository.CSRF_TOKEN");
            oldSession.invalidate();
        }

        // Create new session and store user
        HttpSession newSession = request.getSession(true);
        if (csrfToken != null) {
            newSession.setAttribute(
                    "org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository.CSRF_TOKEN",
                    csrfToken);
        }
        User user = result.getUser();
        newSession.setAttribute("loginUser", user);

        // Set Spring Security authentication context
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
        newSession.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext());

        return "redirect:/home";
    }

    // ---- LOGOUT ----

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return "redirect:/home";
    }

    // ---- REGISTER ----

    @GetMapping("/register")
    public String showRegister(Model model) {
        model.addAttribute("registerForm", new RegisterForm());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerForm") RegisterForm form,
                           BindingResult bindingResult,
                           Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        if (!form.getPassword().equals(form.getPasswordConfirm())) {
            model.addAttribute("errors", List.of("パスワードが一致しません。"));
            return "auth/register";
        }

        if (userService.findByEmail(form.getEmail()) != null) {
            model.addAttribute("errors", List.of("このメールアドレスは既に登録されています。"));
            return "auth/register";
        }

        try {
            User user = new User();
            user.setEmail(form.getEmail());
            user.setUsername(form.getUsername());
            String salt = PasswordHasher.generateSalt();
            String hashed = PasswordHasher.hash(form.getPassword(), salt);
            user.setSalt(salt);
            user.setPasswordHash(hashed);
            userService.register(user);
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("errors", List.of("登録に失敗しました。"));
            return "auth/register";
        }
    }

    // ---- PASSWORD FORGOT ----

    @GetMapping("/password/forgot")
    public String showForgotPassword(Model model) {
        model.addAttribute("passwordResetRequestForm", new PasswordResetRequestForm());
        return "auth/password/forgot";
    }

    @PostMapping("/password/forgot")
    public String forgotPassword(@Valid @ModelAttribute("passwordResetRequestForm") PasswordResetRequestForm form,
                                 BindingResult bindingResult,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/password/forgot";
        }

        // Always show success to prevent user enumeration
        User user = userService.findByEmail(form.getEmail());
        if (user != null) {
            PasswordResetToken token = new PasswordResetToken();
            token.setId(UUID.randomUUID().toString());
            token.setUserId(user.getId());
            token.setToken(UUID.randomUUID().toString());
            token.setExpiresAt(addHours(new Date(), 1));
            token.setUsedAt(null);
            passwordResetTokenDao.insert(token);
            model.addAttribute("resetToken", token.getToken());
            mailService.enqueuePasswordReset(user.getEmail(), token.getToken());
        }

        return "auth/password/forgot_complete";
    }

    // ---- PASSWORD RESET ----

    @GetMapping("/password/reset")
    public String showResetPassword(@RequestParam(value = "token", required = false) String token,
                                    Model model) {
        PasswordResetForm form = new PasswordResetForm();
        if (token != null) {
            form.setToken(token);
        }
        model.addAttribute("passwordResetForm", form);
        return "auth/password/reset";
    }

    @PostMapping("/password/reset")
    public String resetPassword(@Valid @ModelAttribute("passwordResetForm") PasswordResetForm form,
                                BindingResult bindingResult,
                                Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/password/reset";
        }

        if (!form.getPassword().equals(form.getPasswordConfirm())) {
            model.addAttribute("errors", List.of("パスワードが一致しません。"));
            return "auth/password/reset";
        }

        PasswordResetToken token = passwordResetTokenDao.findByToken(form.getToken());
        if (token == null || token.getUsedAt() != null || isExpired(token)) {
            model.addAttribute("errors", List.of("トークンが無効または期限切れです。"));
            return "auth/password/reset";
        }

        User user = userService.findById(token.getUserId());
        if (user == null) {
            model.addAttribute("errors", List.of("トークンが無効または期限切れです。"));
            return "auth/password/reset";
        }

        try {
            String salt = PasswordHasher.generateSalt();
            String hashed = PasswordHasher.hash(form.getPassword(), salt);
            userService.updatePassword(user.getId(), hashed, salt);
            passwordResetTokenDao.markUsed(token.getId());
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("errors", List.of("パスワードのリセットに失敗しました。"));
            return "auth/password/reset";
        }
    }

    // ---- Helpers ----

    private Date addHours(Date base, int hours) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(base);
        cal.add(Calendar.HOUR_OF_DAY, hours);
        return cal.getTime();
    }

    private boolean isExpired(PasswordResetToken token) {
        if (token.getExpiresAt() == null) {
            return true;
        }
        return token.getExpiresAt().before(new Date());
    }
}
