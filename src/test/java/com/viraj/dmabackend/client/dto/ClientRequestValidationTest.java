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

    @Test
    void createClientRequest_WithInvalidGst_FailsValidation() {
        CreateClientRequest request = new CreateClientRequest();
        request.setCompanyName("Acme Corp");
        request.setContactPerson("John Doe");
        request.setEmail("john@example.com");
        request.setPhoneNumber("9876543210");
        request.setGstNumber("INVALID_GST");

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void createClientRequest_WithValidGst_PassesValidation() {
        CreateClientRequest request = new CreateClientRequest();
        request.setCompanyName("Acme Corp");
        request.setContactPerson("John Doe");
        request.setEmail("john@example.com");
        request.setPhoneNumber("9876543210");
        request.setGstNumber("27AAPFU0939F1ZV");

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void createClientRequest_WithInvalidGstChecksum_FailsValidation() {
        CreateClientRequest request = new CreateClientRequest();
        request.setCompanyName("Acme Corp");
        request.setContactPerson("John Doe");
        request.setEmail("john@example.com");
        request.setPhoneNumber("9876543210");
        request.setGstNumber("27AAPFU0939F1ZX"); // 'X' is invalid check digit; valid is 'V'

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void createClientRequest_WithInvalidStateCodeInGst_FailsValidation() {
        CreateClientRequest request = new CreateClientRequest();
        request.setCompanyName("Acme Corp");
        request.setContactPerson("John Doe");
        request.setEmail("john@example.com");
        request.setPhoneNumber("9876543210");

        // 00 is invalid
        request.setGstNumber("00ABCDE1234F1Z5");
        assertFalse(validator.validate(request).isEmpty());

        // 45 is invalid
        request.setGstNumber("45ABCDE1234F1Z5");
        assertFalse(validator.validate(request).isEmpty());

        // 98 is invalid
        request.setGstNumber("98ABCDE1234F1Z5");
        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void createClientRequest_WithSpecialValidStateCodes_PassesValidation() {
        CreateClientRequest request = new CreateClientRequest();
        request.setCompanyName("Acme Corp");
        request.setContactPerson("John Doe");
        request.setEmail("john@example.com");
        request.setPhoneNumber("9876543210");

        request.setGstNumber("97ABCDE1234F1ZT");
        assertTrue(validator.validate(request).isEmpty());

        request.setGstNumber("99ABCDE1234F1ZP");
        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void updateClientRequest_WithNullVersion_FailsValidation() {
        UpdateClientRequest request = new UpdateClientRequest();
        request.setCompanyName("Acme Corp");
        request.setContactPerson("John Doe");
        request.setEmail("john@example.com");
        request.setPhoneNumber("9876543210");
        request.setStatus(com.viraj.dmabackend.client.enums.ClientStatus.ACTIVE);
        request.setVersion(null);

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void updateClientRequest_WithValidVersion_PassesValidation() {
        UpdateClientRequest request = new UpdateClientRequest();
        request.setCompanyName("Acme Corp");
        request.setContactPerson("John Doe");
        request.setEmail("john@example.com");
        request.setPhoneNumber("9876543210");
        request.setStatus(com.viraj.dmabackend.client.enums.ClientStatus.ACTIVE);
        request.setVersion(1L);

        assertTrue(validator.validate(request).isEmpty());
    }
}
