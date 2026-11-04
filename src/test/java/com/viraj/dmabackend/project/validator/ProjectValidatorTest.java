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
}
