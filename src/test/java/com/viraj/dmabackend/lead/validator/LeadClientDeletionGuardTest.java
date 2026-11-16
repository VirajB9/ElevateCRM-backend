package com.viraj.dmabackend.lead.validator;

import com.viraj.dmabackend.exception.BadRequestException;
import com.viraj.dmabackend.lead.repository.LeadRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeadClientDeletionGuardTest {

    @Mock
    private LeadRepository leadRepository;

    @InjectMocks
    private LeadClientDeletionGuard guard;

    @Test
    void validateCanArchiveClient_whenConvertedLeadsExist_throwsBadRequest() {
        when(leadRepository.existsByConvertedClientIdAndDeletedFalse("client-1")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> guard.validateCanArchiveClient("  client-1  "));
    }

    @Test
    void validateCanArchiveClient_whenNoConvertedLeads_passes() {
        when(leadRepository.existsByConvertedClientIdAndDeletedFalse("client-1")).thenReturn(false);

        assertDoesNotThrow(() -> guard.validateCanArchiveClient("client-1"));
        verify(leadRepository).existsByConvertedClientIdAndDeletedFalse("client-1");
    }

    @Test
    void validateCanArchiveClient_whenClientIdIsNull_passes() {
        assertDoesNotThrow(() -> guard.validateCanArchiveClient(null));
    }

    @Test
    void validateCanDeactivateClient_passes() {
        assertDoesNotThrow(() -> guard.validateCanDeactivateClient("client-1"));
    }
}
