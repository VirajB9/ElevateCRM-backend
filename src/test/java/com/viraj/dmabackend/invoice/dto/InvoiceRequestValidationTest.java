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
                .version(1L)
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
                .version(1L)
                .build();

        Set<ConstraintViolation<UpdateInvoiceRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void createInvoiceRequest_whenItemsExceedMax_failsValidation() {
        InvoiceItemRequest item = InvoiceItemRequest.builder()
                .description("Service")
                .quantity(new BigDecimal("1"))
                .unitPrice(new BigDecimal("100.00"))
                .build();

        List<InvoiceItemRequest> items = java.util.Collections.nCopies(101, item);

        CreateInvoiceRequest request = CreateInvoiceRequest.builder()
                .clientId("client-1")
                .projectId("proj-1")
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(15))
                .items(items)
                .taxPercentage(new BigDecimal("18.00"))
                .discount(BigDecimal.ZERO)
                .build();

        Set<ConstraintViolation<CreateInvoiceRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("items")
                && v.getMessage().contains("Invoice cannot contain more than 100 items"));
    }

    @Test
    void updateInvoiceRequest_whenItemsExceedMax_failsValidation() {
        InvoiceItemRequest item = InvoiceItemRequest.builder()
                .description("Service")
                .quantity(new BigDecimal("1"))
                .unitPrice(new BigDecimal("100.00"))
                .build();

        List<InvoiceItemRequest> items = java.util.Collections.nCopies(101, item);

        UpdateInvoiceRequest request = UpdateInvoiceRequest.builder()
                .dueDate(LocalDate.now().plusDays(15))
                .items(items)
                .taxPercentage(new BigDecimal("18.00"))
                .discount(BigDecimal.ZERO)
                .version(1L)
                .build();

        Set<ConstraintViolation<UpdateInvoiceRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("items")
                && v.getMessage().contains("Invoice cannot contain more than 100 items"));
    }

    @Test
    void updateInvoiceRequest_whenVersionIsNull_failsValidation() {
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
                .version(null)
                .build();

        Set<ConstraintViolation<UpdateInvoiceRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("version")
                && v.getMessage().contains("Version is required"));
    }

    @Test
    void updateInvoiceStatusRequest_whenVersionIsNull_failsValidation() {
        UpdateInvoiceStatusRequest request = UpdateInvoiceStatusRequest.builder()
                .status(com.viraj.dmabackend.invoice.enums.InvoiceStatus.SENT)
                .version(null)
                .build();

        Set<ConstraintViolation<UpdateInvoiceStatusRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("version")
                && v.getMessage().contains("Version is required"));
    }

    @Test
    void invoiceItemRequest_whenUnitPriceExceedsAllowedFraction_failsValidation() {
        InvoiceItemRequest item = InvoiceItemRequest.builder()
                .description("Service")
                .quantity(new BigDecimal("1.00"))
                .unitPrice(new BigDecimal("100.123"))
                .build();

        Set<ConstraintViolation<InvoiceItemRequest>> violations = validator.validate(item);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("unitPrice")
                && v.getMessage().contains("Unit price must have at most 15 integer digits and 2 decimal places"));
    }

    @Test
    void invoiceItemRequest_whenUnitPriceExceedsAllowedIntegerDigits_failsValidation() {
        InvoiceItemRequest item = InvoiceItemRequest.builder()
                .description("Service")
                .quantity(new BigDecimal("1.00"))
                .unitPrice(new BigDecimal("1234567890123456.00"))
                .build();

        Set<ConstraintViolation<InvoiceItemRequest>> violations = validator.validate(item);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("unitPrice")
                && v.getMessage().contains("Unit price must have at most 15 integer digits and 2 decimal places"));
    }

    @Test
    void createInvoiceRequest_whenTaxPercentageExceedsAllowedFraction_failsValidation() {
        InvoiceItemRequest item = InvoiceItemRequest.builder()
                .description("Service")
                .quantity(new BigDecimal("1.00"))
                .unitPrice(new BigDecimal("100.00"))
                .build();

        CreateInvoiceRequest request = CreateInvoiceRequest.builder()
                .clientId("client-1")
                .projectId("proj-1")
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(15))
                .items(List.of(item))
                .taxPercentage(new BigDecimal("18.123"))
                .discount(BigDecimal.ZERO)
                .build();

        Set<ConstraintViolation<CreateInvoiceRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("taxPercentage")
                && v.getMessage().contains("Tax percentage must have at most 3 integer digits and 2 decimal places"));
    }

    @Test
    void createInvoiceRequest_whenDiscountExceedsAllowedFraction_failsValidation() {
        InvoiceItemRequest item = InvoiceItemRequest.builder()
                .description("Service")
                .quantity(new BigDecimal("1.00"))
                .unitPrice(new BigDecimal("100.00"))
                .build();

        CreateInvoiceRequest request = CreateInvoiceRequest.builder()
                .clientId("client-1")
                .projectId("proj-1")
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(15))
                .items(List.of(item))
                .taxPercentage(new BigDecimal("18.00"))
                .discount(new BigDecimal("50.999"))
                .build();

        Set<ConstraintViolation<CreateInvoiceRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("discount")
                && v.getMessage().contains("Discount must have at most 15 integer digits and 2 decimal places"));
    }

    @Test
    void updateInvoiceRequest_whenFinancialFieldsExceedAllowedFraction_failsValidation() {
        InvoiceItemRequest item = InvoiceItemRequest.builder()
                .description("Service")
                .quantity(new BigDecimal("1.00"))
                .unitPrice(new BigDecimal("100.00"))
                .build();

        UpdateInvoiceRequest request = UpdateInvoiceRequest.builder()
                .dueDate(LocalDate.now().plusDays(15))
                .items(List.of(item))
                .taxPercentage(new BigDecimal("18.555"))
                .discount(new BigDecimal("10.123"))
                .version(1L)
                .build();

        Set<ConstraintViolation<UpdateInvoiceRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(2);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("taxPercentage")
                && v.getMessage().contains("Tax percentage must have at most 3 integer digits and 2 decimal places"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("discount")
                && v.getMessage().contains("Discount must have at most 15 integer digits and 2 decimal places"));
    }
}
