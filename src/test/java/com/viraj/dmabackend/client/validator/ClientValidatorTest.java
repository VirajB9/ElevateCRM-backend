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

    @Test
    void validateClientStatusForUpdate_ThrowsWhenDeactivatingClientWithActiveProjects() {
        when(projectRepository.existsByClientIdAndStatusIn(eq("client-1"), any()))
                .thenReturn(true);

        assertThrows(BadRequestException.class,
                () -> clientValidator.validateClientStatusForUpdate(com.viraj.dmabackend.client.enums.ClientStatus.INACTIVE, "client-1"));
    }

    @Test
    void validateClientStatusForUpdate_ThrowsWhenDeactivatingClientWithUnpaidInvoices() {
        when(projectRepository.existsByClientIdAndStatusIn(eq("client-1"), any()))
                .thenReturn(false);
        when(invoiceRepository.existsByClientIdAndActiveTrueAndStatusIn(eq("client-1"), any()))
                .thenReturn(true);

        assertThrows(BadRequestException.class,
                () -> clientValidator.validateClientStatusForUpdate(com.viraj.dmabackend.client.enums.ClientStatus.INACTIVE, "client-1"));
    }

    @Test
    void validateClientStatusForUpdate_AllowsDeactivatingClientWithNoActiveDependents() {
        when(projectRepository.existsByClientIdAndStatusIn(eq("client-1"), any()))
                .thenReturn(false);
        when(invoiceRepository.existsByClientIdAndActiveTrueAndStatusIn(eq("client-1"), any()))
                .thenReturn(false);

        assertDoesNotThrow(() -> clientValidator.validateClientStatusForUpdate(com.viraj.dmabackend.client.enums.ClientStatus.INACTIVE, "client-1"));
    }

    @Test
    void validateDuplicateEmail_NormalizesToLowercaseAndTrim() {
        when(clientRepository.existsByEmailAndStatusNot("client@example.com", com.viraj.dmabackend.client.enums.ClientStatus.ARCHIVED))
                .thenReturn(true);

        assertThrows(com.viraj.dmabackend.client.exception.DuplicateClientEmailException.class,
                () -> clientValidator.validateDuplicateEmail("  Client@EXAMPLE.com  "));
    }

    @Test
    void validateEmailForUpdate_NormalizesToLowercaseAndTrim() {
        when(clientRepository.existsByEmailAndIdNotAndStatusNot("client@example.com", "c-1", com.viraj.dmabackend.client.enums.ClientStatus.ARCHIVED))
                .thenReturn(true);

        assertThrows(com.viraj.dmabackend.client.exception.DuplicateClientEmailException.class,
                () -> clientValidator.validateEmailForUpdate("  Client@EXAMPLE.com  ", "c-1"));
    }

    @Test
    void validateDuplicatePhone_NormalizesWhitespace() {
        when(clientRepository.existsByPhoneNumberAndStatusNot("9876543210", com.viraj.dmabackend.client.enums.ClientStatus.ARCHIVED))
                .thenReturn(true);

        assertThrows(com.viraj.dmabackend.client.exception.DuplicateClientPhoneException.class,
                () -> clientValidator.validateDuplicatePhone("  9876543210  "));
    }

    @Test
    void validateDuplicateGst_NormalizesWhitespaceAndUppercase() {
        when(clientRepository.existsByGstNumberAndStatusNot("27ABCDE1234F1Z5", com.viraj.dmabackend.client.enums.ClientStatus.ARCHIVED))
                .thenReturn(true);

        assertThrows(com.viraj.dmabackend.client.exception.DuplicateClientGstException.class,
                () -> clientValidator.validateDuplicateGst("  27abcde1234f1z5  "));
    }

    @Test
    void validatePhoneForUpdate_NormalizesWhitespace() {
        when(clientRepository.existsByPhoneNumberAndIdNotAndStatusNot("9876543210", "c-1", com.viraj.dmabackend.client.enums.ClientStatus.ARCHIVED))
                .thenReturn(true);

        assertThrows(com.viraj.dmabackend.client.exception.DuplicateClientPhoneException.class,
                () -> clientValidator.validatePhoneForUpdate("  9876543210  ", "c-1"));
    }

    @Test
    void validateGstForUpdate_NormalizesWhitespaceAndUppercase() {
        when(clientRepository.existsByGstNumberAndIdNotAndStatusNot("27ABCDE1234F1Z5", "c-1", com.viraj.dmabackend.client.enums.ClientStatus.ARCHIVED))
                .thenReturn(true);

        assertThrows(com.viraj.dmabackend.client.exception.DuplicateClientGstException.class,
                () -> clientValidator.validateGstForUpdate("  27abcde1234f1z5  ", "c-1"));
    }
}
