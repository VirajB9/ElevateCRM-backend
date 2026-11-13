package com.viraj.dmabackend.client.validator;

import com.viraj.dmabackend.client.repository.ClientRepository;
import com.viraj.dmabackend.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientValidatorTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientDeletionGuard clientDeletionGuard;

    private ClientValidator clientValidator;

    @BeforeEach
    void setUp() {
        clientValidator = new ClientValidator(clientRepository, List.of(clientDeletionGuard));
    }

    @Test
    void validateClientDeletable_ThrowsWhenGuardThrows() {
        doThrow(new BadRequestException("Cannot archive client with active or ongoing projects."))
                .when(clientDeletionGuard).validateCanArchiveClient("client-1");

        assertThrows(BadRequestException.class, () -> clientValidator.validateClientDeletable("client-1"));
    }

    @Test
    void validateClientDeletable_PassesWhenNoActiveDependents() {
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
    void validateClientStatusForUpdate_ThrowsWhenGuardThrowsOnDeactivation() {
        doThrow(new BadRequestException("Cannot deactivate client with active or ongoing projects."))
                .when(clientDeletionGuard).validateCanDeactivateClient("client-1");

        assertThrows(BadRequestException.class,
                () -> clientValidator.validateClientStatusForUpdate(com.viraj.dmabackend.client.enums.ClientStatus.INACTIVE, "client-1"));
    }

    @Test
    void validateClientStatusForUpdate_AllowsDeactivatingClientWithNoActiveDependents() {
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
        when(clientRepository.existsByGstNumberAndStatusNot("27AAPFU0939F1ZV", com.viraj.dmabackend.client.enums.ClientStatus.ARCHIVED))
                .thenReturn(true);

        assertThrows(com.viraj.dmabackend.client.exception.DuplicateClientGstException.class,
                () -> clientValidator.validateDuplicateGst("  27aapfu0939f1zv  "));
    }

    @Test
    void validateDuplicateGst_ThrowsWhenChecksumInvalid() {
        assertThrows(BadRequestException.class,
                () -> clientValidator.validateDuplicateGst("27AAPFU0939F1ZX"));
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
        when(clientRepository.existsByGstNumberAndIdNotAndStatusNot("27AAPFU0939F1ZV", "c-1", com.viraj.dmabackend.client.enums.ClientStatus.ARCHIVED))
                .thenReturn(true);

        assertThrows(com.viraj.dmabackend.client.exception.DuplicateClientGstException.class,
                () -> clientValidator.validateGstForUpdate("  27aapfu0939f1zv  ", "  c-1  "));
    }

    @Test
    void validateGstForUpdate_ThrowsWhenChecksumInvalid() {
        assertThrows(BadRequestException.class,
                () -> clientValidator.validateGstForUpdate("27AAPFU0939F1ZX", "c-1"));
    }

    @Test
    void validateEmailForUpdate_TrimsClientId() {
        when(clientRepository.existsByEmailAndIdNotAndStatusNot("test@example.com", "c-1", com.viraj.dmabackend.client.enums.ClientStatus.ARCHIVED))
                .thenReturn(true);

        assertThrows(com.viraj.dmabackend.client.exception.DuplicateClientEmailException.class,
                () -> clientValidator.validateEmailForUpdate("  test@example.com  ", "  c-1  "));
    }

    @Test
    void validatePhoneForUpdate_TrimsClientId() {
        when(clientRepository.existsByPhoneNumberAndIdNotAndStatusNot("9876543210", "c-1", com.viraj.dmabackend.client.enums.ClientStatus.ARCHIVED))
                .thenReturn(true);

        assertThrows(com.viraj.dmabackend.client.exception.DuplicateClientPhoneException.class,
                () -> clientValidator.validatePhoneForUpdate("9876543210", "  c-1  "));
    }

    @Test
    void validateClientStatusForUpdate_TrimsClientId() {
        doThrow(new BadRequestException("Cannot deactivate client with active or ongoing projects."))
                .when(clientDeletionGuard).validateCanDeactivateClient("c-1");

        assertThrows(BadRequestException.class,
                () -> clientValidator.validateClientStatusForUpdate(com.viraj.dmabackend.client.enums.ClientStatus.INACTIVE, "  c-1  "));
    }

    @Test
    void validateClientDeletable_PassesWithTrimmedClientId() {
        assertDoesNotThrow(() -> clientValidator.validateClientDeletable("  c-1  "));
        verify(clientDeletionGuard).validateCanArchiveClient("  c-1  ");
    }
}
