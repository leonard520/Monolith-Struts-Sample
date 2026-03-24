package com.skishop.web.form;

import org.springframework.validation.Errors;

public final class FormValidationUtils {
    private FormValidationUtils() {}

    public static void addPasswordMismatch(Errors errors, String field, String password, String passwordConfirm) {
        if (password != null && passwordConfirm != null && !password.isEmpty() && !passwordConfirm.isEmpty()) {
            if (!password.equals(passwordConfirm)) {
                errors.rejectValue(field, "error.password.mismatch", "パスワードが一致しません");
            }
        }
    }
}
