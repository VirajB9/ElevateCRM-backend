package com.viraj.dmabackend.project.validator;

import com.viraj.dmabackend.auth.validator.UserDeletionGuard;
import com.viraj.dmabackend.exception.BadRequestException;
import com.viraj.dmabackend.project.enums.ProjectStatus;
import com.viraj.dmabackend.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProjectUserDeletionGuard implements UserDeletionGuard {

    private static final List<ProjectStatus> ACTIVE_STATUSES = List.of(
            ProjectStatus.PLANNING,
            ProjectStatus.ACTIVE,
            ProjectStatus.ON_HOLD
    );

    private final ProjectRepository projectRepository;

    @Override
    public void validateCanDeleteUser(String userId) {
        String normalizedUserId = userId != null ? userId.trim() : null;
        if (normalizedUserId != null && projectRepository.existsByCreatedByAndStatusIn(normalizedUserId, ACTIVE_STATUSES)) {
            throw new BadRequestException("Cannot delete user associated with active projects. Please reassign the projects first.");
        }
    }

    @Override
    public void validateCanDeactivateUser(String userId) {
        String normalizedUserId = userId != null ? userId.trim() : null;
        if (normalizedUserId != null && projectRepository.existsByCreatedByAndStatusIn(normalizedUserId, ACTIVE_STATUSES)) {
            throw new BadRequestException("Cannot deactivate user associated with active projects. Please reassign the projects first.");
        }
    }
}
