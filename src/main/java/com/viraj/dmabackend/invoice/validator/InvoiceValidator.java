package com.viraj.dmabackend.invoice.validator;

import com.viraj.dmabackend.client.service.ClientService;
import com.viraj.dmabackend.exception.BadRequestException;
import com.viraj.dmabackend.invoice.dto.CreateInvoiceRequest;
import com.viraj.dmabackend.invoice.dto.InvoiceItemRequest;
import com.viraj.dmabackend.invoice.dto.UpdateInvoiceRequest;
import com.viraj.dmabackend.invoice.entity.Invoice;
import com.viraj.dmabackend.invoice.exception.InvalidInvoiceDateException;
import com.viraj.dmabackend.project.dto.ProjectResponse;
import com.viraj.dmabackend.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InvoiceValidator {

    private final ClientService clientService;
    private final ProjectService projectService;

    public void validateCreateInvoice(CreateInvoiceRequest request) {

        validateClientActiveForInvoice(request.getClientId());

        validateProjectBelongsToClient(request.getProjectId(), request.getClientId());

        validateDates(request.getIssueDate(), request.getDueDate());

        validateDiscount(request.getItems(), request.getDiscount());
    }

    public void validateUpdateInvoice(Invoice invoice, UpdateInvoiceRequest request) {

        if (invoice.getClientId() != null) {
            validateClientActiveForInvoice(invoice.getClientId());
        }

        if (invoice.getProjectId() != null && invoice.getClientId() != null) {
            validateProjectBelongsToClient(invoice.getProjectId(), invoice.getClientId());
        }

        validateDates(invoice.getIssueDate(), request.getDueDate());

        validateDiscount(request.getItems(), request.getDiscount());
    }

    public void validateClientExists(String clientId) {
        clientService.getClientById(clientId != null ? clientId.trim() : null);
    }

    public void validateProjectExists(String projectId) {
        projectService.getProjectById(projectId != null ? projectId.trim() : null);
    }

    private void validateClientActiveForInvoice(String clientId) {
        String trimmedClientId = clientId != null ? clientId.trim() : null;
        com.viraj.dmabackend.client.dto.ClientResponse client = clientService.getClientById(trimmedClientId);
        if (client.getStatus() != com.viraj.dmabackend.client.enums.ClientStatus.ACTIVE) {
            throw new BadRequestException("Cannot create an invoice for an inactive client.");
        }
    }

    private void validateProjectBelongsToClient(String projectId, String clientId) {
        String trimmedProjectId = projectId != null ? projectId.trim() : null;
        String trimmedClientId = clientId != null ? clientId.trim() : null;
        ProjectResponse project = projectService.getProjectById(trimmedProjectId);

        String projectClientId = project.getClientId() != null ? project.getClientId().trim() : null;
        if (trimmedClientId == null || !trimmedClientId.equals(projectClientId)) {
            throw new BadRequestException("Project does not belong to the selected client");
        }

        if (project.getStatus() == com.viraj.dmabackend.project.enums.ProjectStatus.CANCELLED) {
            throw new BadRequestException("Cannot create an invoice for a cancelled project.");
        }
    }

    private void validateDates(LocalDate issueDate, LocalDate dueDate) {

        if (issueDate == null || dueDate == null) {
            return;
        }

        if (dueDate.isBefore(issueDate)) {
            throw new InvalidInvoiceDateException();
        }
    }

    private void validateDiscount(List<InvoiceItemRequest> items, BigDecimal discount) {

        if (discount == null) {
            return;
        }

        if (discount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Discount cannot be negative");
        }

        if (items == null || items.isEmpty()) {
            return;
        }

        BigDecimal subtotal = items.stream()
                .filter(item -> item != null && item.getQuantity() != null && item.getUnitPrice() != null)
                .map(item -> {
                    BigDecimal unitPrice = item.getUnitPrice().setScale(2, java.math.RoundingMode.HALF_UP);
                    return item.getQuantity()
                            .multiply(unitPrice)
                            .setScale(2, java.math.RoundingMode.HALF_UP);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (discount.compareTo(subtotal) > 0) {
            throw new BadRequestException("Discount cannot be greater than subtotal");
        }
    }
}
