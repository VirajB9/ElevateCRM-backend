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
        String normalizedClientId = clientId != null ? clientId.trim() : null;
        if (!clientRepository.existsByIdAndStatusNot(normalizedClientId, com.viraj.dmabackend.client.enums.ClientStatus.ARCHIVED)) {
            throw new ClientNotFoundException(clientId);
        }
    }

    public void validateClientActiveForProject(String clientId) {
        String normalizedClientId = clientId != null ? clientId.trim() : null;
        com.viraj.dmabackend.client.entity.Client client = clientRepository.findByIdAndStatusNot(normalizedClientId, com.viraj.dmabackend.client.enums.ClientStatus.ARCHIVED)
                .orElseThrow(() -> new ClientNotFoundException(clientId));
        if (client.getStatus() != com.viraj.dmabackend.client.enums.ClientStatus.ACTIVE) {
            throw new BadRequestException("Cannot create a project for an inactive client.");
        }
    }

    public void validateDuplicateProject(String projectName, String clientId) {
        String normalizedProjectName = projectName != null ? projectName.trim() : null;
        String normalizedClientId = clientId != null ? clientId.trim() : null;
        if (projectRepository.existsByProjectNameAndClientIdAndStatusNot(normalizedProjectName, normalizedClientId, ProjectStatus.ARCHIVED)) {
            throw new DuplicateProjectException(normalizedProjectName, normalizedClientId);
        }
    }

    public void validateProjectDates(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new InvalidProjectDateException();
        }
    }

    public void validateDuplicateProjectForUpdate(String projectName, String clientId, String projectId) {
        String normalizedProjectName = projectName != null ? projectName.trim() : null;
        String normalizedClientId = clientId != null ? clientId.trim() : null;
        String normalizedProjectId = projectId != null ? projectId.trim() : null;
        if (projectRepository.existsByProjectNameAndClientIdAndIdNotAndStatusNot(normalizedProjectName, normalizedClientId, normalizedProjectId, ProjectStatus.ARCHIVED)) {
            throw new DuplicateProjectException(normalizedProjectName, normalizedClientId);
        }
    }

    public void validateProjectDeletable(String projectId) {
        String normalizedProjectId = projectId != null ? projectId.trim() : null;
        if (invoiceRepository.existsByProjectIdAndActiveTrueAndStatusIn(normalizedProjectId, 
                List.of(InvoiceStatus.DRAFT, InvoiceStatus.SENT, InvoiceStatus.OVERDUE))) {
            throw new BadRequestException("Cannot archive project with unpaid or active invoices.");
        }
    }

    public void validateProjectEditable(ProjectStatus currentStatus) {
        if (currentStatus == ProjectStatus.CANCELLED) {
            throw new BadRequestException("Cancelled projects cannot be edited.");
        }
        if (currentStatus == ProjectStatus.COMPLETED) {
            throw new BadRequestException("Completed projects cannot be edited.");
        }
    }

    public void validateProjectStatusForUpdate(ProjectStatus status) {
        if (status == ProjectStatus.ARCHIVED) {
            throw new BadRequestException("Projects cannot be archived via update. Please use the delete project endpoint.");
        }
    }

    public void validateProjectStatusTransition(ProjectStatus currentStatus, ProjectStatus newStatus) {
        if (currentStatus == null || newStatus == null || currentStatus == newStatus) {
            return;
        }

        if (currentStatus == ProjectStatus.CANCELLED) {
            throw new BadRequestException("Cancelled projects cannot change status.");
        }

        if (currentStatus == ProjectStatus.COMPLETED) {
            throw new BadRequestException("Completed projects cannot change status.");
        }

        if (newStatus == ProjectStatus.ARCHIVED) {
            throw new BadRequestException("Projects cannot be archived via update. Please use the delete project endpoint.");
        }

        if (newStatus == ProjectStatus.PLANNING && currentStatus != ProjectStatus.PLANNING) {
            throw new BadRequestException("Cannot revert an active or on-hold project back to planning status.");
        }

        switch (currentStatus) {
            case PLANNING -> {
                if (newStatus == ProjectStatus.COMPLETED) {
                    throw new BadRequestException("Planning projects cannot be marked completed directly without being active.");
                }
            }
            case ON_HOLD -> {
                if (newStatus == ProjectStatus.COMPLETED) {
                    throw new BadRequestException("On-hold projects cannot be marked completed directly. Reactivate the project first.");
                }
            }
            default -> {
            }
        }
    }
}
