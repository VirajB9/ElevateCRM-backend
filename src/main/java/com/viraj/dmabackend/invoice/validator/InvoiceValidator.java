package com.viraj.dmabackend.invoice.validator;

import com.viraj.dmabackend.client.exception.ClientNotFoundException;
import com.viraj.dmabackend.client.repository.ClientRepository;
import com.viraj.dmabackend.client.service.ClientService;
import com.viraj.dmabackend.exception.BadRequestException;
import com.viraj.dmabackend.invoice.dto.CreateInvoiceRequest;
import com.viraj.dmabackend.invoice.dto.InvoiceItemRequest;
import com.viraj.dmabackend.invoice.dto.UpdateInvoiceRequest;
import com.viraj.dmabackend.invoice.entity.Invoice;
import com.viraj.dmabackend.invoice.exception.InvalidInvoiceDateException;
import com.viraj.dmabackend.project.dto.ProjectResponse;
import com.viraj.dmabackend.project.exception.ProjectNotFoundException;
import com.viraj.dmabackend.project.repository.ProjectRepository;
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
    private final ClientRepository clientRepository;
    private final ProjectRepository projectRepository;

    public void validateCreateInvoice(CreateInvoiceRequest request) {

        validateItems(request.getItems());

        validateClientActiveForInvoice(request.getClientId(), "Cannot create an invoice for an inactive client.");

        validateProjectBelongsToClient(request.getProjectId(), request.getClientId(), "create");

        validateDates(request.getIssueDate(), request.getDueDate());

        validateDiscount(request.getItems(), request.getDiscount());
    }

    public void validateUpdateInvoice(Invoice invoice, UpdateInvoiceRequest request) {

        validateItems(request.getItems());

        if (invoice.getClientId() != null) {
            validateClientActiveForInvoice(invoice.getClientId(), "Cannot update an invoice for an inactive client.");
        }

        if (invoice.getProjectId() != null && invoice.getClientId() != null) {
            validateProjectBelongsToClient(invoice.getProjectId(), invoice.getClientId(), "update");
        }

        validateDates(invoice.getIssueDate(), request.getDueDate());

        validateDiscount(request.getItems(), request.getDiscount());
    }

    private void validateItems(List<InvoiceItemRequest> items) {
        if (items != null && !items.isEmpty() && items.stream().noneMatch(java.util.Objects::nonNull)) {
            throw new BadRequestException("Invoice must contain at least one valid line item");
        }
        if (items != null) {
            for (InvoiceItemRequest item : items) {
                if (item != null && item.getUnitPrice() != null && item.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new BadRequestException("Line item unit price must be greater than zero");
                }
            }
        }
    }

    public void validateClientExists(String clientId) {
        String trimmedClientId = clientId != null ? clientId.trim() : null;
        if (trimmedClientId == null || !clientRepository.existsById(trimmedClientId)) {
            throw new ClientNotFoundException(clientId);
        }
    }

    public void validateProjectExists(String projectId) {
        String trimmedProjectId = projectId != null ? projectId.trim() : null;
        if (trimmedProjectId == null || !projectRepository.existsById(trimmedProjectId)) {
            throw new ProjectNotFoundException(projectId);
        }
    }

    private void validateClientActiveForInvoice(String clientId, String errorMessage) {
        String trimmedClientId = clientId != null ? clientId.trim() : null;
        com.viraj.dmabackend.client.dto.ClientResponse client = clientService.getClientById(trimmedClientId);
        if (client.getStatus() != com.viraj.dmabackend.client.enums.ClientStatus.ACTIVE) {
            throw new BadRequestException(errorMessage);
        }
    }

    public void validateProjectBelongsToClient(String projectId, String clientId, String action) {
        String trimmedProjectId = projectId != null ? projectId.trim() : null;
        String trimmedClientId = clientId != null ? clientId.trim() : null;
        ProjectResponse project = projectService.getProjectById(trimmedProjectId);

        String projectClientId = project.getClientId() != null ? project.getClientId().trim() : null;
        if (trimmedClientId == null || !trimmedClientId.equals(projectClientId)) {
            throw new BadRequestException("Project does not belong to the selected client");
        }

        if (project.getStatus() == com.viraj.dmabackend.project.enums.ProjectStatus.CANCELLED) {
            throw new BadRequestException("Cannot " + action + " an invoice for a cancelled project.");
        }

        if (project.getStatus() == com.viraj.dmabackend.project.enums.ProjectStatus.ARCHIVED) {
            throw new BadRequestException("Cannot " + action + " an invoice for an archived project.");
        }

        if (project.getStatus() == com.viraj.dmabackend.project.enums.ProjectStatus.COMPLETED) {
            throw new BadRequestException("Cannot " + action + " an invoice for a completed project.");
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
                        .multiply(item.getUnitPrice())
                        .setScale(2, java.math.RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (discount.compareTo(subtotal) > 0) {
            throw new BadRequestException("Discount cannot be greater than subtotal");
        }
    }
}
