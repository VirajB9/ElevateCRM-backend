package com.viraj.dmabackend.invoice.entity;

import com.viraj.dmabackend.invoice.enums.InvoiceStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "invoice_history")
@TypeAlias("InvoiceHistory")
public class InvoiceHistory {

    @Id
    private String id;

    @Indexed
    private String invoiceId;

    private String invoiceNumber;

    private String clientId;

    private String projectId;

    private LocalDate issueDate;

    private LocalDate dueDate;

    private List<InvoiceItem> items;

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

    @Indexed
    private LocalDateTime changedAt;
}