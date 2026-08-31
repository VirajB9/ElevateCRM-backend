package com.viraj.dmabackend.invoice.dto;

import com.viraj.dmabackend.invoice.enums.InvoiceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse {

    private String id;

    private String invoiceNumber;

    private String clientId;

    private String projectId;

    private LocalDate issueDate;

    private LocalDate dueDate;

    private List<InvoiceItemResponse> items;

    private BigDecimal subtotal;

    private BigDecimal taxPercentage;

    private BigDecimal taxAmount;

    private BigDecimal discount;

    private BigDecimal totalAmount;

    private InvoiceStatus status;

    private String notes;

    private LocalDate paidDate;

    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
