package com.viraj.dmabackend.client.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClientRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void createClientRequest_WithInvalidPhoneNumber_FailsValidation() {
        CreateClientRequest request = new CreateClientRequest();
        request.setCompanyName("Acme Corp");
        request.setContactPerson("John Doe");
        request.setEmail("john@example.com");
        request.setPhoneNumber("invalid-phone");

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void createClientRequest_WithValidPhoneNumber_PassesValidation() {
        CreateClientRequest request = new CreateClientRequest();
        request.setCompanyName("Acme Corp");
        request.setContactPerson("John Doe");
        request.setEmail("john@example.com");
        request.setPhoneNumber("9876543210");

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void updateClientRequest_WithInvalidPhoneNumber_FailsValidation() {
        UpdateClientRequest request = new UpdateClientRequest();
        request.setCompanyName("Acme Corp");
        request.setContactPerson("John Doe");
        request.setEmail("john@example.com");
        request.setPhoneNumber("123");

        assertFalse(validator.validate(request).isEmpty());
    }
}
