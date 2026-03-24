package com.skishop.web.form;

import com.skishop.web.form.admin.AdminCouponForm;
import com.skishop.web.form.admin.AdminProductForm;
import com.skishop.web.form.admin.AdminShippingMethodForm;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AdminFormValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void adminProductForm_allRequired_noViolations() {
        AdminProductForm form = new AdminProductForm();
        form.setName("Test Product");
        form.setPrice("5000");
        Set<ConstraintViolation<AdminProductForm>> violations = validator.validate(form);
        assertTrue(violations.isEmpty());
    }

    @Test
    void adminProductForm_blankNameAndPrice_violations() {
        AdminProductForm form = new AdminProductForm();
        Set<ConstraintViolation<AdminProductForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());

        Set<String> violatedFields = new HashSet<>();
        violations.forEach(v -> violatedFields.add(v.getPropertyPath().toString()));
        assertTrue(violatedFields.contains("name"));
        assertTrue(violatedFields.contains("price"));
    }

    @Test
    void adminCouponForm_validCodeAndDiscount_noViolations() {
        AdminCouponForm form = new AdminCouponForm();
        form.setCode("SAVE10");
        form.setDiscountValue("10");
        Set<ConstraintViolation<AdminCouponForm>> violations = validator.validate(form);
        assertTrue(violations.isEmpty());
    }

    @Test
    void adminCouponForm_blankCodeAndDiscount_violations() {
        AdminCouponForm form = new AdminCouponForm();
        Set<ConstraintViolation<AdminCouponForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());

        Set<String> violatedFields = new HashSet<>();
        violations.forEach(v -> violatedFields.add(v.getPropertyPath().toString()));
        assertTrue(violatedFields.contains("code"));
        assertTrue(violatedFields.contains("discountValue"));
    }

    @Test
    void adminShippingMethodForm_validFields_noViolations() {
        AdminShippingMethodForm form = new AdminShippingMethodForm();
        form.setCode("STANDARD");
        form.setName("Standard Shipping");
        form.setFee("500");
        Set<ConstraintViolation<AdminShippingMethodForm>> violations = validator.validate(form);
        assertTrue(violations.isEmpty());
    }

    @Test
    void adminShippingMethodForm_blankFields_violations() {
        AdminShippingMethodForm form = new AdminShippingMethodForm();
        Set<ConstraintViolation<AdminShippingMethodForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());

        Set<String> violatedFields = new HashSet<>();
        violations.forEach(v -> violatedFields.add(v.getPropertyPath().toString()));
        assertTrue(violatedFields.contains("code"));
        assertTrue(violatedFields.contains("name"));
        assertTrue(violatedFields.contains("fee"));
    }
}
