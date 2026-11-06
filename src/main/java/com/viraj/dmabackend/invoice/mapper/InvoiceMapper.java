package com.viraj.dmabackend.invoice.mapper;

import com.viraj.dmabackend.invoice.dto.*;
import com.viraj.dmabackend.invoice.entity.Invoice;
import com.viraj.dmabackend.invoice.entity.InvoiceHistory;
import com.viraj.dmabackend.invoice.entity.InvoiceItem;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InvoiceMapper {

    public Invoice toEntity(CreateInvoiceRequest request) {
        if (request == null) {
            return null;
        }
        return Invoice.builder()
                .clientId(normalizeTrim(request.getClientId()))
                .projectId(normalizeTrim(request.getProjectId()))
                .issueDate(request.getIssueDate())
                .dueDate(request.getDueDate())
                .items(toEntityItems(request.getItems()))
                .taxPercentage(request.getTaxPercentage())
                .discount(request.getDiscount())
                .notes(normalizeTrim(request.getNotes()))
                .build();
    }

    public void updateEntity(Invoice invoice, UpdateInvoiceRequest request) {
        if (invoice == null || request == null) {
            return;
        }
        invoice.setDueDate(request.getDueDate());
        invoice.setItems(toEntityItems(request.getItems()));
        invoice.setTaxPercentage(request.getTaxPercentage());
        invoice.setDiscount(request.getDiscount());
        invoice.setNotes(normalizeTrim(request.getNotes()));
    }

    public InvoiceResponse toResponse(Invoice invoice) {
        if (invoice == null) {
            return null;
        }
        return InvoiceResponse.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .clientId(invoice.getClientId())
                .projectId(invoice.getProjectId())
                .issueDate(invoice.getIssueDate())
                .dueDate(invoice.getDueDate())
                .items(toResponseItems(invoice.getItems()))
                .subtotal(invoice.getSubtotal())
                .taxPercentage(invoice.getTaxPercentage())
                .taxAmount(invoice.getTaxAmount())
                .discount(invoice.getDiscount())
                .totalAmount(invoice.getTotalAmount())
                .status(invoice.getStatus())
                .notes(invoice.getNotes())
                .paidDate(invoice.getPaidDate())
                .active(invoice.getActive())
                .createdAt(invoice.getCreatedAt())
                .updatedAt(invoice.getUpdatedAt())
                .createdBy(invoice.getCreatedBy())
                .updatedBy(invoice.getUpdatedBy())
                .build();
    }

    private List<InvoiceItem> toEntityItems(List<InvoiceItemRequest> items) {
        if (items == null) {
            return java.util.Collections.emptyList();
        }
        return items.stream()
                .filter(java.util.Objects::nonNull)
                .map(this::toEntityItem)
                .toList();
    }

    private InvoiceItem toEntityItem(InvoiceItemRequest itemRequest) {
        return InvoiceItem.builder()
                .description(itemRequest.getDescription() != null ? itemRequest.getDescription().trim() : null)
                .quantity(itemRequest.getQuantity())
                .unitPrice(itemRequest.getUnitPrice())
                .build();
    }

    private List<InvoiceItemResponse> toResponseItems(List<InvoiceItem> items) {
        if (items == null) {
            return java.util.Collections.emptyList();
        }
        return items.stream()
                .filter(java.util.Objects::nonNull)
                .map(this::toResponseItem)
                .toList();
    }

    private InvoiceItemResponse toResponseItem(InvoiceItem item) {
        return InvoiceItemResponse.builder()
                .description(item.getDescription())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .lineTotal(item.getLineTotal())
                .build();
    }

    public InvoiceHistoryResponse toHistoryResponse(InvoiceHistory history) {
        if (history == null) {
            return null;
        }
        return InvoiceHistoryResponse.builder()
                .id(history.getId())
                .invoiceId(history.getInvoiceId())
                .invoiceNumber(history.getInvoiceNumber())
                .clientId(history.getClientId())
                .projectId(history.getProjectId())
                .issueDate(history.getIssueDate())
                .dueDate(history.getDueDate())
                .items(history.getItems() != null ? toResponseItems(history.getItems()) : java.util.Collections.emptyList())
                .subtotal(history.getSubtotal())
                .taxPercentage(history.getTaxPercentage())
                .taxAmount(history.getTaxAmount())
                .discount(history.getDiscount())
                .totalAmount(history.getTotalAmount())
                .status(history.getStatus())
                .notes(history.getNotes())
                .paidDate(history.getPaidDate())
                .active(history.getActive())
                .invoiceVersion(history.getInvoiceVersion())
                .changedBy(history.getChangedBy())
                .changedAt(history.getChangedAt())
                .build();
    }

    private String normalizeTrim(String value) {
        return value != null ? value.trim() : null;
    }
}
