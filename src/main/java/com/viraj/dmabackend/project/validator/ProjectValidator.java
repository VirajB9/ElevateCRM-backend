package com.viraj.dmabackend.project.validator;

import com.viraj.dmabackend.client.dto.ClientResponse;
import com.viraj.dmabackend.client.enums.ClientStatus;
import com.viraj.dmabackend.client.service.ClientService;
import com.viraj.dmabackend.exception.BadRequestException;
import com.viraj.dmabackend.project.entity.Project;
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
    private final ClientService clientService;
    private final List<ProjectDeletionGuard> projectDeletionGuards;

    public void validateClientExists(String clientId) {
        String normalizedClientId = clientId != null ? clientId.trim() : null;
        clientService.getClientById(normalizedClientId);
    }

    public void validateClientActiveForProject(String clientId) {
        String normalizedClientId = clientId != null ? clientId.trim() : null;
        ClientResponse client = clientService.getClientById(normalizedClientId);
        if (client.getStatus() != ClientStatus.ACTIVE) {
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

    public void validateProjectCompletion(LocalDate endDate) {
        if (endDate == null) {
            throw new BadRequestException("Completion date is required when completing a project.");
        }
        if (endDate.isAfter(LocalDate.now(java.time.ZoneOffset.UTC))) {
            throw new BadRequestException("Completion date cannot be in the future.");
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

    public void validateProjectDeletable(Project project) {
        if (project.getStatus() != ProjectStatus.COMPLETED && project.getStatus() != ProjectStatus.CANCELLED) {
            throw new BadRequestException("Only completed or cancelled projects can be archived. Current status is " + project.getStatus());
        }
        validateProjectDeletable(project.getId());
    }

    public void validateProjectDeletable(String projectId) {
        if (projectDeletionGuards != null) {
            for (ProjectDeletionGuard guard : projectDeletionGuards) {
                guard.validateCanArchiveProject(projectId);
            }
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
