package com.viraj.dmabackend.invoice.dto;

import com.viraj.dmabackend.invoice.enums.InvoiceStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceHistoryResponse {

    private String id;
    private String invoiceId;
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
    private Long invoiceVersion;
    private String changedBy;
    private LocalDateTime changedAt;
}
