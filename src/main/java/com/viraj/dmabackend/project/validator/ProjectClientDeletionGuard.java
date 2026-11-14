package com.viraj.dmabackend.project.validator;

import com.viraj.dmabackend.client.validator.ClientDeletionGuard;
import com.viraj.dmabackend.exception.BadRequestException;
import com.viraj.dmabackend.project.enums.ProjectStatus;
import com.viraj.dmabackend.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProjectClientDeletionGuard implements ClientDeletionGuard {

    private final ProjectRepository projectRepository;

    @Override
    public void validateCanArchiveClient(String clientId) {
        String trimmedClientId = clientId != null ? clientId.trim() : null;
        if (trimmedClientId != null && projectRepository.existsByClientIdAndStatusIn(trimmedClientId,
                List.of(ProjectStatus.PLANNING, ProjectStatus.ACTIVE, ProjectStatus.ON_HOLD))) {
            throw new BadRequestException("Cannot archive client with active or ongoing projects.");
        }
    }

    @Override
    public void validateCanDeactivateClient(String clientId) {
        String trimmedClientId = clientId != null ? clientId.trim() : null;
        if (trimmedClientId != null && projectRepository.existsByClientIdAndStatusIn(trimmedClientId,
                List.of(ProjectStatus.PLANNING, ProjectStatus.ACTIVE, ProjectStatus.ON_HOLD))) {
            throw new BadRequestException("Cannot deactivate client with active or ongoing projects.");
        }
    }
}
