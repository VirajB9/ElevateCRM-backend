package com.viraj.dmabackend.invoice.mapper;

import com.viraj.dmabackend.invoice.dto.*;
import com.viraj.dmabackend.invoice.entity.Invoice;
import com.viraj.dmabackend.invoice.entity.InvoiceItem;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InvoiceMapper {

    public Invoice toEntity(CreateInvoiceRequest request) {
        return Invoice.builder()
                .clientId(request.getClientId())
                .projectId(request.getProjectId())
                .issueDate(request.getIssueDate())
                .dueDate(request.getDueDate())
                .items(toEntityItems(request.getItems()))
                .taxPercentage(request.getTaxPercentage())
                .discount(request.getDiscount())
                .notes(request.getNotes())
                .build();
    }

    public void updateEntity(Invoice invoice, UpdateInvoiceRequest request) {
        invoice.setDueDate(request.getDueDate());
        invoice.setItems(toEntityItems(request.getItems()));
        invoice.setTaxPercentage(request.getTaxPercentage());
        invoice.setDiscount(request.getDiscount());
        invoice.setNotes(request.getNotes());
    }

    public InvoiceResponse toResponse(Invoice invoice) {
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
                .build();
    }

    private List<InvoiceItem> toEntityItems(List<InvoiceItemRequest> items) {
        return items.stream()
                .map(this::toEntityItem)
                .toList();
    }

    private InvoiceItem toEntityItem(InvoiceItemRequest itemRequest) {
        return InvoiceItem.builder()
                .description(itemRequest.getDescription())
                .quantity(itemRequest.getQuantity())
                .unitPrice(itemRequest.getUnitPrice())
                .build();
    }

    private List<InvoiceItemResponse> toResponseItems(List<InvoiceItem> items) {
        return items.stream()
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
}
