package com.viraj.dmabackend.project.validator;

import com.viraj.dmabackend.client.exception.ClientNotFoundException;
import com.viraj.dmabackend.client.repository.ClientRepository;
import com.viraj.dmabackend.exception.BadRequestException;
import com.viraj.dmabackend.invoice.enums.InvoiceStatus;
import com.viraj.dmabackend.invoice.repository.InvoiceRepository;
import com.viraj.dmabackend.project.enums.ProjectStatus;
import com.viraj.dmabackend.project.exception.DuplicateProjectException;
import com.viraj.dmabackend.project.exception.InvalidProjectDateException;
import com.viraj.dmabackend.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProjectValidator {

    private final ProjectRepository projectRepository;
    private final ClientRepository clientRepository;
    private final InvoiceRepository invoiceRepository;

    public void validateClientExists(String clientId) {
        if (!clientRepository.existsByIdAndStatusNot(clientId, com.viraj.dmabackend.client.enums.ClientStatus.ARCHIVED)) {
            throw new ClientNotFoundException(clientId);
        }
    }

    public void validateDuplicateProject(String projectName, String clientId) {
        if (projectRepository.existsByProjectNameAndClientIdAndStatusNot(projectName, clientId, ProjectStatus.ARCHIVED)) {
            throw new DuplicateProjectException(projectName, clientId);
        }
    }

    public void validateProjectDates(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new InvalidProjectDateException();
        }
    }

    public void validateDuplicateProjectForUpdate(String projectName, String clientId, String projectId) {
        if (projectRepository.existsByProjectNameAndClientIdAndIdNotAndStatusNot(projectName, clientId, projectId, ProjectStatus.ARCHIVED)) {
            throw new DuplicateProjectException(projectName, clientId);
        }
    }

    public void validateProjectDeletable(String projectId) {
        if (invoiceRepository.existsByProjectIdAndActiveTrueAndStatusIn(projectId, 
                List.of(InvoiceStatus.DRAFT, InvoiceStatus.SENT, InvoiceStatus.OVERDUE))) {
            throw new BadRequestException("Cannot archive project with unpaid or active invoices.");
        }
    }
}
