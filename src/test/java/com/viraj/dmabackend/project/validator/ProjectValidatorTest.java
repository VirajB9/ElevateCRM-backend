package com.viraj.dmabackend.project.validator;

import com.viraj.dmabackend.client.repository.ClientRepository;
import com.viraj.dmabackend.exception.BadRequestException;
import com.viraj.dmabackend.invoice.repository.InvoiceRepository;
import com.viraj.dmabackend.project.enums.ProjectStatus;
import com.viraj.dmabackend.project.exception.DuplicateProjectException;
import com.viraj.dmabackend.project.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProjectValidatorTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private ProjectValidator projectValidator;

    @Test
    void validateDuplicateProject_AllowsNameWhenArchived() {
        when(projectRepository.existsByProjectNameAndClientIdAndStatusNot("CRM App", "client-123", ProjectStatus.ARCHIVED))
                .thenReturn(false);

        assertDoesNotThrow(() -> projectValidator.validateDuplicateProject("CRM App", "client-123"));
    }

    @Test
    void validateDuplicateProject_ThrowsWhenActiveProjectExists() {
        when(projectRepository.existsByProjectNameAndClientIdAndStatusNot("CRM App", "client-123", ProjectStatus.ARCHIVED))
                .thenReturn(true);

        assertThrows(DuplicateProjectException.class,
                () -> projectValidator.validateDuplicateProject("CRM App", "client-123"));
    }

    @Test
    void validateProjectDeletable_ThrowsWhenUnpaidInvoicesExist() {
        when(invoiceRepository.existsByProjectIdAndActiveTrueAndStatusIn(eq("proj-1"), any()))
                .thenReturn(true);

        assertThrows(BadRequestException.class,
                () -> projectValidator.validateProjectDeletable("proj-1"));
    }

    @Test
    void validateProjectDeletable_PassesWhenNoUnpaidInvoices() {
        when(invoiceRepository.existsByProjectIdAndActiveTrueAndStatusIn(eq("proj-1"), any()))
                .thenReturn(false);

        assertDoesNotThrow(() -> projectValidator.validateProjectDeletable("proj-1"));
    }

    @Test
    void validateProjectEditable_ThrowsWhenCancelled() {
        assertThrows(BadRequestException.class,
                () -> projectValidator.validateProjectEditable(ProjectStatus.CANCELLED));
    }

    @Test
    void validateProjectEditable_ThrowsWhenCompleted() {
        assertThrows(BadRequestException.class,
                () -> projectValidator.validateProjectEditable(ProjectStatus.COMPLETED));
    }

    @Test
    void validateProjectEditable_PassesWhenActiveOrPlanning() {
        assertDoesNotThrow(() -> projectValidator.validateProjectEditable(ProjectStatus.ACTIVE));
        assertDoesNotThrow(() -> projectValidator.validateProjectEditable(ProjectStatus.PLANNING));
        assertDoesNotThrow(() -> projectValidator.validateProjectEditable(ProjectStatus.ON_HOLD));
    }

    @Test
    void validateProjectStatusTransition_ThrowsWhenCurrentStatusIsCompletedOrCancelled() {
        assertThrows(BadRequestException.class,
                () -> projectValidator.validateProjectStatusTransition(ProjectStatus.COMPLETED, ProjectStatus.ACTIVE));
        assertThrows(BadRequestException.class,
                () -> projectValidator.validateProjectStatusTransition(ProjectStatus.CANCELLED, ProjectStatus.ACTIVE));
    }

    @Test
    void validateProjectStatusTransition_ThrowsWhenPlanningDirectToCompleted() {
        assertThrows(BadRequestException.class,
                () -> projectValidator.validateProjectStatusTransition(ProjectStatus.PLANNING, ProjectStatus.COMPLETED));
    }

    @Test
    void validateProjectStatusTransition_ThrowsWhenOnHoldDirectToCompleted() {
        assertThrows(BadRequestException.class,
                () -> projectValidator.validateProjectStatusTransition(ProjectStatus.ON_HOLD, ProjectStatus.COMPLETED));
    }

    @Test
    void validateProjectStatusTransition_ThrowsWhenRevertingToPlanning() {
        assertThrows(BadRequestException.class,
                () -> projectValidator.validateProjectStatusTransition(ProjectStatus.ACTIVE, ProjectStatus.PLANNING));
        assertThrows(BadRequestException.class,
                () -> projectValidator.validateProjectStatusTransition(ProjectStatus.ON_HOLD, ProjectStatus.PLANNING));
    }

    @Test
    void validateProjectStatusTransition_ValidTransitionsPass() {
        assertDoesNotThrow(() -> projectValidator.validateProjectStatusTransition(ProjectStatus.PLANNING, ProjectStatus.ACTIVE));
        assertDoesNotThrow(() -> projectValidator.validateProjectStatusTransition(ProjectStatus.ACTIVE, ProjectStatus.COMPLETED));
        assertDoesNotThrow(() -> projectValidator.validateProjectStatusTransition(ProjectStatus.ACTIVE, ProjectStatus.ON_HOLD));
        assertDoesNotThrow(() -> projectValidator.validateProjectStatusTransition(ProjectStatus.ON_HOLD, ProjectStatus.ACTIVE));
        assertDoesNotThrow(() -> projectValidator.validateProjectStatusTransition(ProjectStatus.ACTIVE, ProjectStatus.ACTIVE));
    }

    @Test
    void validateProjectStatusForUpdate_ThrowsWhenArchived() {
        assertThrows(BadRequestException.class,
                () -> projectValidator.validateProjectStatusForUpdate(ProjectStatus.ARCHIVED));
    }

    @Test
    void validateProjectStatusForUpdate_AllowsActiveStatus() {
        assertDoesNotThrow(() -> projectValidator.validateProjectStatusForUpdate(ProjectStatus.ACTIVE));
        assertDoesNotThrow(() -> projectValidator.validateProjectStatusForUpdate(ProjectStatus.COMPLETED));
        assertDoesNotThrow(() -> projectValidator.validateProjectStatusForUpdate(ProjectStatus.ON_HOLD));
    }

    @Test
    void validateClientActiveForProject_PassesWhenActive() {
        com.viraj.dmabackend.client.entity.Client client =
                com.viraj.dmabackend.client.entity.Client.builder()
                        .id("client-1")
                        .status(com.viraj.dmabackend.client.enums.ClientStatus.ACTIVE)
                        .build();

        when(clientRepository.findByIdAndStatusNot("client-1", com.viraj.dmabackend.client.enums.ClientStatus.ARCHIVED))
                .thenReturn(java.util.Optional.of(client));

        assertDoesNotThrow(() -> projectValidator.validateClientActiveForProject("client-1"));
    }

    @Test
    void validateClientActiveForProject_ThrowsWhenInactive() {
        com.viraj.dmabackend.client.entity.Client client =
                com.viraj.dmabackend.client.entity.Client.builder()
                        .id("client-2")
                        .status(com.viraj.dmabackend.client.enums.ClientStatus.INACTIVE)
                        .build();

        when(clientRepository.findByIdAndStatusNot("client-2", com.viraj.dmabackend.client.enums.ClientStatus.ARCHIVED))
                .thenReturn(java.util.Optional.of(client));

        assertThrows(BadRequestException.class,
                () -> projectValidator.validateClientActiveForProject("client-2"));
    }

    @Test
    void validateDuplicateProject_TrimsProjectNameBeforeQuery() {
        when(projectRepository.existsByProjectNameAndClientIdAndStatusNot("CRM App", "client-123", ProjectStatus.ARCHIVED))
                .thenReturn(true);

        assertThrows(DuplicateProjectException.class,
                () -> projectValidator.validateDuplicateProject("  CRM App  ", "client-123"));
    }

    @Test
    void validateDuplicateProjectForUpdate_TrimsProjectNameBeforeQuery() {
        when(projectRepository.existsByProjectNameAndClientIdAndIdNotAndStatusNot("CRM App", "client-123", "proj-9", ProjectStatus.ARCHIVED))
                .thenReturn(true);

        assertThrows(DuplicateProjectException.class,
                () -> projectValidator.validateDuplicateProjectForUpdate("  CRM App  ", "client-123", "proj-9"));
    }
}
