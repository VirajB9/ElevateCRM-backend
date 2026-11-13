package com.viraj.dmabackend.auth.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void updateUserRequest_invalidPhoneNumber_failsValidation() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setPhoneNumber("12345");
        request.setRoleId("role-1");

        Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("phoneNumber")
                && v.getMessage().contains("10-digit Indian mobile number"));
    }

    @Test
    void updateUserRequest_validPhoneNumber_passesValidation() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setPhoneNumber("9876543210");
        request.setRoleId("role-1");
        request.setVersion(1L);

        Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void updateUserRequest_nullVersion_failsValidation() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setPhoneNumber("9876543210");
        request.setRoleId("role-1");
        request.setVersion(null);

        Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("version")
                && v.getMessage().contains("Version is required"));
    }
}
