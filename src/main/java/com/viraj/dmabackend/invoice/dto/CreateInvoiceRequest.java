package com.viraj.dmabackend.invoice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateInvoiceRequest {

    @NotBlank(message = "Client ID is required")
    private String clientId;

    @NotBlank(message = "Project ID is required")
    private String projectId;

    @NotNull(message = "Issue date is required")
    private LocalDate issueDate;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    @NotEmpty(message = "At least one invoice item is required")
    @Valid
    private List<InvoiceItemRequest> items;

    @NotNull(message = "Tax percentage is required")
    @DecimalMin(value = "0.00", message = "Tax percentage cannot be negative")
    private BigDecimal taxPercentage;

    @NotNull(message = "Discount is required")
    @DecimalMin(value = "0.00", message = "Discount cannot be negative")
    private BigDecimal discount;

    private String notes;
}
