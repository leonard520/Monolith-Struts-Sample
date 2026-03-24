package com.skishop.web.form;

import jakarta.validation.constraints.NotBlank;

public class PasswordResetForm {
    @NotBlank private String token;
    @NotBlank private String password;
    @NotBlank private String passwordConfirm;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getPasswordConfirm() { return passwordConfirm; }
    public void setPasswordConfirm(String passwordConfirm) { this.passwordConfirm = passwordConfirm; }
}
