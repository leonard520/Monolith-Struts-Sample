package com.skishop.web.form;

import jakarta.validation.constraints.NotBlank;

public class RegisterForm {
    @NotBlank private String email;
    @NotBlank private String username;
    @NotBlank private String password;
    @NotBlank private String passwordConfirm;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getPasswordConfirm() { return passwordConfirm; }
    public void setPasswordConfirm(String passwordConfirm) { this.passwordConfirm = passwordConfirm; }
}
