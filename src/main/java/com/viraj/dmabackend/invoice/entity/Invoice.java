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
@CompoundIndex(name = "idx_client_deleted", def = "{'clientId': 1, 'deleted': 1}")
@CompoundIndex(name = "idx_status_deleted", def = "{'status': 1, 'deleted': 1}")
@CompoundIndex(name = "idx_project_deleted", def = "{'projectId': 1, 'deleted': 1}")
@CompoundIndex(name = "idx_deleted_created_at", def = "{'deleted': 1, 'createdAt': -1}")
@CompoundIndex(name = "idx_invoice_client_deleted_status", def = "{'clientId': 1, 'deleted': 1, 'status': 1}")
@CompoundIndex(name = "idx_invoice_project_deleted_status", def = "{'projectId': 1, 'deleted': 1, 'status': 1}")
public class Invoice extends BaseEntity {

    @Id
    private String id;

    @Indexed(unique = true)
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

    @Builder.Default
    private Boolean active = true;

    public Boolean getActive() {
        return !isDeleted();
    }

    public void setActive(Boolean active) {
        this.active = active;
        setDeleted(active != null && !active);
    }
}
