package com.viraj.dmabackend.project.validator;

import com.viraj.dmabackend.exception.BadRequestException;
import com.viraj.dmabackend.project.enums.ProjectStatus;
import com.viraj.dmabackend.project.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectUserDeletionGuardTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectUserDeletionGuard guard;

    private static final List<ProjectStatus> ACTIVE_STATUSES = List.of(
            ProjectStatus.PLANNING,
            ProjectStatus.ACTIVE,
            ProjectStatus.ON_HOLD
    );

    @Test
    void validateCanDeleteUser_whenUserHasActiveProjects_throwsBadRequestException() {
        when(projectRepository.existsByCreatedByAndStatusIn(eq("u-1"), eq(ACTIVE_STATUSES)))
                .thenReturn(true);

        assertThrows(BadRequestException.class, () -> guard.validateCanDeleteUser("  u-1  "));
    }

    @Test
    void validateCanDeleteUser_whenUserHasNoActiveProjects_passes() {
        when(projectRepository.existsByCreatedByAndStatusIn(eq("u-1"), eq(ACTIVE_STATUSES)))
                .thenReturn(false);

        assertDoesNotThrow(() -> guard.validateCanDeleteUser("u-1"));
        verify(projectRepository).existsByCreatedByAndStatusIn(eq("u-1"), eq(ACTIVE_STATUSES));
    }

    @Test
    void validateCanDeleteUser_whenUserIdIsNull_passes() {
        assertDoesNotThrow(() -> guard.validateCanDeleteUser(null));
    }

    @Test
    void validateCanDeactivateUser_whenUserHasActiveProjects_throwsBadRequestException() {
        when(projectRepository.existsByCreatedByAndStatusIn(eq("u-1"), eq(ACTIVE_STATUSES)))
                .thenReturn(true);

        assertThrows(BadRequestException.class, () -> guard.validateCanDeactivateUser("  u-1  "));
    }

    @Test
    void validateCanDeactivateUser_whenUserHasNoActiveProjects_passes() {
        when(projectRepository.existsByCreatedByAndStatusIn(eq("u-1"), eq(ACTIVE_STATUSES)))
                .thenReturn(false);

        assertDoesNotThrow(() -> guard.validateCanDeactivateUser("u-1"));
        verify(projectRepository).existsByCreatedByAndStatusIn(eq("u-1"), eq(ACTIVE_STATUSES));
    }
}
