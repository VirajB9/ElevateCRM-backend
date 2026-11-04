package com.viraj.dmabackend.invoice.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class InvoiceRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void updateInvoiceRequest_whenDueDateIsNull_failsValidation() {
        InvoiceItemRequest item = InvoiceItemRequest.builder()
                .description("Service")
                .quantity(new BigDecimal("1"))
                .unitPrice(new BigDecimal("100.00"))
                .build();

        UpdateInvoiceRequest request = UpdateInvoiceRequest.builder()
                .dueDate(null)
                .items(List.of(item))
                .taxPercentage(BigDecimal.ZERO)
                .discount(BigDecimal.ZERO)
                .build();

        Set<ConstraintViolation<UpdateInvoiceRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("dueDate")
                && v.getMessage().contains("Due date is required"));
    }

    @Test
    void updateInvoiceRequest_whenValid_passesValidation() {
        InvoiceItemRequest item = InvoiceItemRequest.builder()
                .description("Service")
                .quantity(new BigDecimal("1"))
                .unitPrice(new BigDecimal("100.00"))
                .build();

        UpdateInvoiceRequest request = UpdateInvoiceRequest.builder()
                .dueDate(LocalDate.now().plusDays(15))
                .items(List.of(item))
                .taxPercentage(new BigDecimal("18.00"))
                .discount(BigDecimal.ZERO)
                .build();

        Set<ConstraintViolation<UpdateInvoiceRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }
}
