package com.skishop.web.form;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class P2FormValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void addressForm_allRequiredFields_noViolations() {
        AddressForm form = new AddressForm();
        form.setLabel("Home");
        form.setRecipientName("Test User");
        form.setPostalCode("100-0001");
        form.setPrefecture("Tokyo");
        form.setAddress1("Chiyoda 1-1");

        Set<ConstraintViolation<AddressForm>> violations = validator.validate(form);
        assertTrue(violations.isEmpty());
    }

    @Test
    void addressForm_blankRequiredFields_violations() {
        AddressForm form = new AddressForm();
        Set<ConstraintViolation<AddressForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());

        Set<String> violatedFields = new java.util.HashSet<>();
        violations.forEach(v -> violatedFields.add(v.getPropertyPath().toString()));
        assertTrue(violatedFields.contains("label"));
        assertTrue(violatedFields.contains("recipientName"));
        assertTrue(violatedFields.contains("postalCode"));
        assertTrue(violatedFields.contains("prefecture"));
        assertTrue(violatedFields.contains("address1"));
    }

    @Test
    void passwordResetRequestForm_validEmail_noViolations() {
        PasswordResetRequestForm form = new PasswordResetRequestForm();
        form.setEmail("user@example.com");
        Set<ConstraintViolation<PasswordResetRequestForm>> violations = validator.validate(form);
        assertTrue(violations.isEmpty());
    }

    @Test
    void passwordResetRequestForm_blankEmail_violation() {
        PasswordResetRequestForm form = new PasswordResetRequestForm();
        Set<ConstraintViolation<PasswordResetRequestForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
    }

    @Test
    void passwordResetRequestForm_invalidEmail_violation() {
        PasswordResetRequestForm form = new PasswordResetRequestForm();
        form.setEmail("not-an-email");
        Set<ConstraintViolation<PasswordResetRequestForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
    }

    @Test
    void passwordResetForm_blankTokenOrPassword_violations() {
        PasswordResetForm form = new PasswordResetForm();
        Set<ConstraintViolation<PasswordResetForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());

        Set<String> violatedFields = new java.util.HashSet<>();
        violations.forEach(v -> violatedFields.add(v.getPropertyPath().toString()));
        assertTrue(violatedFields.contains("token"));
        assertTrue(violatedFields.contains("password"));
        assertTrue(violatedFields.contains("passwordConfirm"));
    }

    @Test
    void passwordResetForm_allFieldsFilled_noViolations() {
        PasswordResetForm form = new PasswordResetForm();
        form.setToken("valid-token");
        form.setPassword("newpassword");
        form.setPasswordConfirm("newpassword");
        Set<ConstraintViolation<PasswordResetForm>> violations = validator.validate(form);
        assertTrue(violations.isEmpty());
    }
}
