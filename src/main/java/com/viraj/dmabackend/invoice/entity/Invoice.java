package com.viraj.dmabackend.invoice.entity;

import com.viraj.dmabackend.common.entity.BaseEntity;
import com.viraj.dmabackend.invoice.enums.InvoiceStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "invoices")
@TypeAlias("Invoice")
@CompoundIndex(name = "idx_client_active", def = "{'clientId': 1, 'active': 1}")
@CompoundIndex(name = "idx_status_active", def = "{'status': 1, 'active': 1}")
@CompoundIndex(name = "idx_project_active", def = "{'projectId': 1, 'active': 1}")
@CompoundIndex(name = "idx_active_created_at", def = "{'active': 1, 'createdAt': -1}")
public class Invoice extends BaseEntity {

    @Id
    private String id;

    @Indexed(unique = true)
    private String invoiceNumber;

    @Indexed
    private String clientId;

    @Indexed
    private String projectId;

    private LocalDate issueDate;

    private LocalDate dueDate;

    private List<InvoiceItem> items;

    private BigDecimal subtotal;

    private BigDecimal taxPercentage;

    private BigDecimal taxAmount;

    private BigDecimal discount;

    private BigDecimal totalAmount;

    @Indexed
    private InvoiceStatus status;

    private String notes;

    private LocalDate paidDate;

    @Indexed
    private Boolean active;
}
