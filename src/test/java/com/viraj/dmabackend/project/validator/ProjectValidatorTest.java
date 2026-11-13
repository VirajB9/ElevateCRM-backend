package com.viraj.dmabackend.project.validator;

import com.viraj.dmabackend.client.dto.ClientResponse;
import com.viraj.dmabackend.client.enums.ClientStatus;
import com.viraj.dmabackend.client.service.ClientService;
import com.viraj.dmabackend.exception.BadRequestException;
import com.viraj.dmabackend.project.entity.Project;
import com.viraj.dmabackend.project.enums.ProjectStatus;
import com.viraj.dmabackend.project.exception.DuplicateProjectException;
import com.viraj.dmabackend.project.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProjectValidatorTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ClientService clientService;

    @Mock
    private ProjectDeletionGuard projectDeletionGuard;

    @Mock
    private ProjectCompletionGuard projectCompletionGuard;

    private ProjectValidator projectValidator;

    @BeforeEach
    void setUp() {
        projectValidator = new ProjectValidator(projectRepository, clientService, List.of(projectDeletionGuard), List.of(projectCompletionGuard));
    }

    @Test
    void validateClientExists_TrimsClientIdBeforeQueryingRepository() {
        when(clientService.getClientById("client-123"))
                .thenReturn(ClientResponse.builder().id("client-123").build());

        assertDoesNotThrow(() -> projectValidator.validateClientExists("  client-123  "));
    }

    @Test
    void validateClientActiveForProject_TrimsClientIdBeforeQueryingRepository() {
        ClientResponse client = ClientResponse.builder().id("client-123").status(ClientStatus.ACTIVE).build();
        when(clientService.getClientById("client-123"))
                .thenReturn(client);

        assertDoesNotThrow(() -> projectValidator.validateClientActiveForProject("  client-123  "));
    }

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
        doThrow(new BadRequestException("Cannot archive project with unpaid or active invoices."))
                .when(projectDeletionGuard).validateCanArchiveProject("proj-1");

        assertThrows(BadRequestException.class,
                () -> projectValidator.validateProjectDeletable("proj-1"));
    }

    @Test
    void validateProjectDeletable_ThrowsWhenProjectIsActiveOrPlanning() {
        Project activeProject = Project.builder().id("p1").status(ProjectStatus.ACTIVE).build();
        Project planningProject = Project.builder().id("p2").status(ProjectStatus.PLANNING).build();
        Project onHoldProject = Project.builder().id("p3").status(ProjectStatus.ON_HOLD).build();

        assertThrows(BadRequestException.class, () -> projectValidator.validateProjectDeletable(activeProject));
        assertThrows(BadRequestException.class, () -> projectValidator.validateProjectDeletable(planningProject));
        assertThrows(BadRequestException.class, () -> projectValidator.validateProjectDeletable(onHoldProject));
    }

    @Test
    void validateProjectDeletable_PassesWhenCompletedOrCancelledAndNoUnpaidInvoices() {
        Project completed = Project.builder().id("p1").status(ProjectStatus.COMPLETED).build();
        Project cancelled = Project.builder().id("p2").status(ProjectStatus.CANCELLED).build();

        assertDoesNotThrow(() -> projectValidator.validateProjectDeletable(completed));
        assertDoesNotThrow(() -> projectValidator.validateProjectDeletable(cancelled));
    }

    @Test
    void validateProjectDeletable_PassesWhenNoUnpaidInvoices() {
        assertDoesNotThrow(() -> projectValidator.validateProjectDeletable("proj-1"));
        verify(projectDeletionGuard).validateCanArchiveProject("proj-1");
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
    void validateProjectStatusTransition_ThrowsWhenPlanningDirectToOnHold() {
        assertThrows(BadRequestException.class,
                () -> projectValidator.validateProjectStatusTransition(ProjectStatus.PLANNING, ProjectStatus.ON_HOLD));
    }

    @Test
    void validateActiveProjectDates_ThrowsWhenStartDateInFuture() {
        LocalDate futureDate = LocalDate.now(java.time.ZoneOffset.UTC).plusDays(5);
        assertThrows(BadRequestException.class,
                () -> projectValidator.validateActiveProjectDates(ProjectStatus.ACTIVE, futureDate, null));
    }

    @Test
    void validateActiveProjectDates_ThrowsWhenEndDateInPast() {
        LocalDate pastDate = LocalDate.now(java.time.ZoneOffset.UTC).minusDays(2);
        assertThrows(BadRequestException.class,
                () -> projectValidator.validateActiveProjectDates(ProjectStatus.ACTIVE, LocalDate.now(java.time.ZoneOffset.UTC), pastDate));
    }

    @Test
    void validateProjectCompletionGuards_DelegatesToGuards() {
        doNothing().when(projectCompletionGuard).validateCanCompleteProject("proj-1");
        assertDoesNotThrow(() -> projectValidator.validateProjectCompletionGuards("proj-1"));
        verify(projectCompletionGuard).validateCanCompleteProject("proj-1");
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
        ClientResponse client = ClientResponse.builder()
                .id("client-1")
                .status(ClientStatus.ACTIVE)
                .build();

        when(clientService.getClientById("client-1")).thenReturn(client);

        assertDoesNotThrow(() -> projectValidator.validateClientActiveForProject("client-1"));
    }

    @Test
    void validateClientActiveForProject_ThrowsWhenInactive() {
        ClientResponse client = ClientResponse.builder()
                .id("client-2")
                .status(ClientStatus.INACTIVE)
                .build();

        when(clientService.getClientById("client-2")).thenReturn(client);

        assertThrows(BadRequestException.class,
                () -> projectValidator.validateClientActiveForProject("client-2"));
    }

    @Test
    void validateDuplicateProject_TrimsProjectNameBeforeQuery() {
        when(projectRepository.existsByProjectNameAndClientIdAndStatusNot("CRM App", "client-123", ProjectStatus.ARCHIVED))
                .thenReturn(true);

        assertThrows(DuplicateProjectException.class,
                () -> projectValidator.validateDuplicateProject("  CRM App  ", "  client-123  "));
    }

    @Test
    void validateDuplicateProjectForUpdate_TrimsProjectNameBeforeQuery() {
        when(projectRepository.existsByProjectNameAndClientIdAndIdNotAndStatusNot("CRM App", "client-123", "proj-9", ProjectStatus.ARCHIVED))
                .thenReturn(true);

        assertThrows(DuplicateProjectException.class,
                () -> projectValidator.validateDuplicateProjectForUpdate("  CRM App  ", "  client-123  ", "  proj-9  "));
    }

    @Test
    void validateProjectCompletion_NullEndDate_ThrowsBadRequest() {
        assertThrows(BadRequestException.class,
                () -> projectValidator.validateProjectCompletion(null));
    }

    @Test
    void validateProjectCompletion_FutureEndDate_ThrowsBadRequest() {
        assertThrows(BadRequestException.class,
                () -> projectValidator.validateProjectCompletion(LocalDate.now(java.time.ZoneOffset.UTC).plusDays(2)));
    }

    @Test
    void validateProjectCompletion_TodayOrPastEndDate_Passes() {
        assertDoesNotThrow(() -> projectValidator.validateProjectCompletion(LocalDate.now(java.time.ZoneOffset.UTC)));
        assertDoesNotThrow(() -> projectValidator.validateProjectCompletion(LocalDate.now(java.time.ZoneOffset.UTC).minusDays(1)));
    }
}
