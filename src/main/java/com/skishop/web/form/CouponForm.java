package com.skishop.web.form;

import jakarta.validation.constraints.NotBlank;

public class CouponForm {
    @NotBlank private String code;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
