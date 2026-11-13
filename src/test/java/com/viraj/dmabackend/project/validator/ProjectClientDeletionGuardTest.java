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
class ProjectClientDeletionGuardTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectClientDeletionGuard guard;

    @Test
    void validateCanArchiveClient_whenActiveProjectsExist_throwsBadRequest() {
        when(projectRepository.existsByClientIdAndStatusIn(eq("client-1"), eq(List.of(
                ProjectStatus.PLANNING, ProjectStatus.ACTIVE, ProjectStatus.ON_HOLD))))
                .thenReturn(true);

        assertThrows(BadRequestException.class, () -> guard.validateCanArchiveClient("  client-1  "));
    }

    @Test
    void validateCanArchiveClient_whenNoActiveProjects_passes() {
        when(projectRepository.existsByClientIdAndStatusIn(eq("client-1"), eq(List.of(
                ProjectStatus.PLANNING, ProjectStatus.ACTIVE, ProjectStatus.ON_HOLD))))
                .thenReturn(false);

        assertDoesNotThrow(() -> guard.validateCanArchiveClient("client-1"));
        verify(projectRepository).existsByClientIdAndStatusIn(eq("client-1"), eq(List.of(
                ProjectStatus.PLANNING, ProjectStatus.ACTIVE, ProjectStatus.ON_HOLD)));
    }

    @Test
    void validateCanDeactivateClient_whenActiveProjectsExist_throwsBadRequest() {
        when(projectRepository.existsByClientIdAndStatusIn(eq("client-1"), eq(List.of(
                ProjectStatus.ACTIVE))))
                .thenReturn(true);

        assertThrows(BadRequestException.class, () -> guard.validateCanDeactivateClient("  client-1  "));
    }

    @Test
    void validateCanDeactivateClient_whenNoActiveProjects_passes() {
        when(projectRepository.existsByClientIdAndStatusIn(eq("client-1"), eq(List.of(
                ProjectStatus.ACTIVE))))
                .thenReturn(false);

        assertDoesNotThrow(() -> guard.validateCanDeactivateClient("client-1"));
    }

    @Test
    void validateCanDeactivateClient_whenProjectsAreOnHoldOrPlanning_passes() {
        when(projectRepository.existsByClientIdAndStatusIn(eq("client-1"), eq(List.of(
                ProjectStatus.ACTIVE))))
                .thenReturn(false);

        assertDoesNotThrow(() -> guard.validateCanDeactivateClient("client-1"));
    }

    @Test
    void validateCanArchiveClient_whenClientIdIsNull_passes() {
        assertDoesNotThrow(() -> guard.validateCanArchiveClient(null));
    }
}
