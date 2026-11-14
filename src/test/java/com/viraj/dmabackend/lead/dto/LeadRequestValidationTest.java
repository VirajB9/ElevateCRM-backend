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

    @Test
    void createLeadRequest_WithExcessiveBudgetFraction_FailsValidation() {
        CreateLeadRequest request = new CreateLeadRequest();
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setCompanyName("Acme");
        request.setEmail("jane@example.com");
        request.setPhoneNumber("9876543210");
        request.setSource(LeadSource.LINKEDIN);
        request.setEstimatedBudget(new java.math.BigDecimal("5000.999"));

        var violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("estimatedBudget")
                && v.getMessage().contains("at most 15 integer digits and 2 decimal places")));
    }

    @Test
    void updateLeadRequest_WithExcessiveBudgetInteger_FailsValidation() {
        UpdateLeadRequest request = new UpdateLeadRequest();
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setCompanyName("Acme");
        request.setEmail("jane@example.com");
        request.setPhoneNumber("9876543210");
        request.setSource(LeadSource.LINKEDIN);
        request.setStatus(LeadStatus.NEW);
        request.setVersion(1L);
        request.setEstimatedBudget(new java.math.BigDecimal("10000000000000000.00")); // 17 digits

        var violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("estimatedBudget")
                && v.getMessage().contains("at most 15 integer digits and 2 decimal places")));
    }
}
