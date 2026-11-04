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

        validateClientExists(request.getClientId());

        validateProjectBelongsToClient(request.getProjectId(), request.getClientId());

        validateDates(request.getIssueDate(), request.getDueDate());

        validateDiscount(request.getItems(), request.getDiscount());
    }

    public void validateUpdateInvoice(Invoice invoice, UpdateInvoiceRequest request) {

        validateDates(invoice.getIssueDate(), request.getDueDate());

        validateDiscount(request.getItems(), request.getDiscount());
    }

    private void validateClientExists(String clientId) {
        com.viraj.dmabackend.client.dto.ClientResponse client = clientService.getClientById(clientId);
        if (client.getStatus() != com.viraj.dmabackend.client.enums.ClientStatus.ACTIVE) {
            throw new BadRequestException("Cannot create an invoice for an inactive client.");
        }
    }

    private void validateProjectBelongsToClient(String projectId, String clientId) {
        ProjectResponse project = projectService.getProjectById(projectId);

        if (!clientId.equals(project.getClientId())) {
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
                .map(item -> item.getQuantity()
                        .multiply(item.getUnitPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (discount.compareTo(subtotal) > 0) {
            throw new BadRequestException("Discount cannot be greater than subtotal");
        }
    }
}
