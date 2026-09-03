package com.viraj.dmabackend.invoice.validator;

import com.viraj.dmabackend.client.repository.ClientRepository;
import com.viraj.dmabackend.exception.BadRequestException;
import com.viraj.dmabackend.exception.ResourceNotFoundException;
import com.viraj.dmabackend.invoice.dto.CreateInvoiceRequest;
import com.viraj.dmabackend.invoice.dto.InvoiceItemRequest;
import com.viraj.dmabackend.invoice.dto.UpdateInvoiceRequest;
import com.viraj.dmabackend.invoice.entity.Invoice;
import com.viraj.dmabackend.invoice.exception.InvalidInvoiceDateException;
import com.viraj.dmabackend.project.entity.Project;
import com.viraj.dmabackend.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InvoiceValidator {

    private final ClientRepository clientRepository;
    private final ProjectRepository projectRepository;

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

        if (!clientRepository.existsByIdAndStatusNot(clientId, com.viraj.dmabackend.client.enums.ClientStatus.ARCHIVED)) {
            throw new ResourceNotFoundException("Client not found with id: " + clientId);
        }
    }

    private void validateProjectBelongsToClient(String projectId, String clientId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Project not found with id: " + projectId));

        if (!clientId.equals(project.getClientId())) {
            throw new BadRequestException("Project does not belong to the selected client");
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

        if (discount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Discount cannot be negative");
        }

        BigDecimal subtotal = items.stream()
                .map(item -> item.getQuantity()
                        .multiply(item.getUnitPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (discount.compareTo(subtotal) > 0) {
            throw new BadRequestException("Discount cannot be greater than subtotal");
        }
    }
}
