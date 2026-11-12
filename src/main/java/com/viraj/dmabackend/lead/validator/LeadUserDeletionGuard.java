package com.viraj.dmabackend.lead.validator;

import com.viraj.dmabackend.auth.validator.UserDeletionGuard;
import com.viraj.dmabackend.exception.BadRequestException;
import com.viraj.dmabackend.lead.enums.LeadStatus;
import com.viraj.dmabackend.lead.repository.LeadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LeadUserDeletionGuard implements UserDeletionGuard {

    private final LeadRepository leadRepository;

    @Override
    public void validateCanDeleteUser(String userId) {
        String normalizedUserId = userId != null ? userId.trim() : null;
        if (normalizedUserId != null && leadRepository.existsByAssignedUserIdAndStatusIn(normalizedUserId,
                List.of(LeadStatus.NEW, LeadStatus.CONTACTED, LeadStatus.QUALIFIED, LeadStatus.PROPOSAL_SENT, LeadStatus.NEGOTIATION))) {
            throw new BadRequestException("Cannot delete user with active assigned leads. Please reassign the leads first.");
        }
    }
}
