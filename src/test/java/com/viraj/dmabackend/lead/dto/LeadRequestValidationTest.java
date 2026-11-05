package com.viraj.dmabackend.lead.dto;

import com.viraj.dmabackend.lead.enums.LeadSource;
import com.viraj.dmabackend.lead.enums.LeadStatus;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LeadRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void createLeadRequest_WithInvalidPhoneNumber_FailsValidation() {
        CreateLeadRequest request = new CreateLeadRequest();
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setCompanyName("Acme");
        request.setEmail("jane@example.com");
        request.setPhoneNumber("letters-only");
        request.setSource(LeadSource.LINKEDIN);

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void createLeadRequest_WithValidPhoneNumber_PassesValidation() {
        CreateLeadRequest request = new CreateLeadRequest();
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setCompanyName("Acme");
        request.setEmail("jane@example.com");
        request.setPhoneNumber("9876543210");
        request.setSource(LeadSource.LINKEDIN);

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void updateLeadRequest_WithInvalidPhoneNumber_FailsValidation() {
        UpdateLeadRequest request = new UpdateLeadRequest();
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setCompanyName("Acme");
        request.setEmail("jane@example.com");
        request.setPhoneNumber("999");
        request.setSource(LeadSource.LINKEDIN);
        request.setStatus(LeadStatus.NEW);

        assertFalse(validator.validate(request).isEmpty());
    }
}
