package com.viraj.dmabackend.invoice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
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
public class UpdateInvoiceRequest {

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    @NotEmpty(message = "At least one invoice item is required")
    @jakarta.validation.constraints.Size(max = 100, message = "Invoice cannot contain more than 100 items")
    @Valid
    private List<InvoiceItemRequest> items;

    @NotNull(message = "Tax percentage is required")
    @DecimalMin(value = "0.00", message = "Tax percentage cannot be negative")
    @DecimalMax(value = "100.00", message = "Tax percentage cannot exceed 100")
    private BigDecimal taxPercentage;

    @NotNull(message = "Discount is required")
    @DecimalMin(value = "0.00", message = "Discount cannot be negative")
    private BigDecimal discount;

    
    @jakarta.validation.constraints.Size(max = 2000)
    private String notes;

    @NotNull(message = "Version is required for optimistic concurrency control")
    private Long version;
}
