package com.viraj.dmabackend.auth.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChangePasswordRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validate_PasswordShorterThan8Chars_FailsValidation() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldPassword123");
        request.setNewPassword("1234567"); // 7 chars, less than 8

        Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("newPassword")));
    }

    @Test
    void validate_PasswordWith8OrMoreChars_PassesValidation() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldPassword123");
        request.setNewPassword("newPassword123"); // 14 chars

        Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }
}
