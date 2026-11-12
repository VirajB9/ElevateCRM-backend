package com.viraj.dmabackend.lead.validator;

import com.viraj.dmabackend.exception.BadRequestException;
import com.viraj.dmabackend.lead.enums.LeadStatus;
import com.viraj.dmabackend.lead.repository.LeadRepository;
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
class LeadUserDeletionGuardTest {

    @Mock
    private LeadRepository leadRepository;

    @InjectMocks
    private LeadUserDeletionGuard guard;

    @Test
    void validateCanDeleteUser_whenUserHasActiveLeads_throwsBadRequestException() {
        when(leadRepository.existsByAssignedUserIdAndStatusIn(eq("u-1"), eq(List.of(
                LeadStatus.NEW, LeadStatus.CONTACTED, LeadStatus.QUALIFIED, LeadStatus.PROPOSAL_SENT, LeadStatus.NEGOTIATION))))
                .thenReturn(true);

        assertThrows(BadRequestException.class, () -> guard.validateCanDeleteUser("  u-1  "));
    }

    @Test
    void validateCanDeleteUser_whenUserHasNoActiveLeads_passes() {
        when(leadRepository.existsByAssignedUserIdAndStatusIn(eq("u-1"), eq(List.of(
                LeadStatus.NEW, LeadStatus.CONTACTED, LeadStatus.QUALIFIED, LeadStatus.PROPOSAL_SENT, LeadStatus.NEGOTIATION))))
                .thenReturn(false);

        assertDoesNotThrow(() -> guard.validateCanDeleteUser("u-1"));
        verify(leadRepository).existsByAssignedUserIdAndStatusIn(eq("u-1"), eq(List.of(
                LeadStatus.NEW, LeadStatus.CONTACTED, LeadStatus.QUALIFIED, LeadStatus.PROPOSAL_SENT, LeadStatus.NEGOTIATION)));
    }

    @Test
    void validateCanDeleteUser_whenUserIdIsNull_passes() {
        assertDoesNotThrow(() -> guard.validateCanDeleteUser(null));
    }
}
