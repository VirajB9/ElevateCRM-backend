package com.viraj.dmabackend.client.validator;

import com.viraj.dmabackend.client.repository.ClientRepository;
import com.viraj.dmabackend.exception.BadRequestException;
import com.viraj.dmabackend.invoice.repository.InvoiceRepository;
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
public class ClientValidatorTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private ClientValidator clientValidator;

    @Test
    void validateClientDeletable_ThrowsWhenActiveProjectsExist() {
        when(projectRepository.existsByClientIdAndStatusIn(eq("client-1"), any()))
                .thenReturn(true);

        assertThrows(BadRequestException.class, () -> clientValidator.validateClientDeletable("client-1"));
    }

    @Test
    void validateClientDeletable_ThrowsWhenUnpaidInvoicesExist() {
        when(projectRepository.existsByClientIdAndStatusIn(eq("client-1"), any()))
                .thenReturn(false);
        when(invoiceRepository.existsByClientIdAndActiveTrueAndStatusIn(eq("client-1"), any()))
                .thenReturn(true);

        assertThrows(BadRequestException.class, () -> clientValidator.validateClientDeletable("client-1"));
    }

    @Test
    void validateClientDeletable_PassesWhenNoActiveDependents() {
        when(projectRepository.existsByClientIdAndStatusIn(eq("client-1"), any()))
                .thenReturn(false);
        when(invoiceRepository.existsByClientIdAndActiveTrueAndStatusIn(eq("client-1"), any()))
                .thenReturn(false);

        assertDoesNotThrow(() -> clientValidator.validateClientDeletable("client-1"));
    }

    @Test
    void validateClientStatusForUpdate_ThrowsWhenArchived() {
        assertThrows(BadRequestException.class,
                () -> clientValidator.validateClientStatusForUpdate(com.viraj.dmabackend.client.enums.ClientStatus.ARCHIVED));
    }

    @Test
    void validateClientStatusForUpdate_AllowsActiveAndInactive() {
        assertDoesNotThrow(() -> clientValidator.validateClientStatusForUpdate(com.viraj.dmabackend.client.enums.ClientStatus.ACTIVE));
        assertDoesNotThrow(() -> clientValidator.validateClientStatusForUpdate(com.viraj.dmabackend.client.enums.ClientStatus.INACTIVE));
    }
}
