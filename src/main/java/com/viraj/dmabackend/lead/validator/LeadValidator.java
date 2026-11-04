package com.viraj.dmabackend.lead.validator;

import com.viraj.dmabackend.auth.entity.User;
import com.viraj.dmabackend.auth.enums.UserStatus;
import com.viraj.dmabackend.auth.exception.UserNotFoundException;
import com.viraj.dmabackend.auth.repository.UserRepository;
import com.viraj.dmabackend.exception.BadRequestException;
import com.viraj.dmabackend.lead.dto.CreateLeadRequest;
import com.viraj.dmabackend.lead.dto.UpdateLeadRequest;
import com.viraj.dmabackend.lead.entity.Lead;
import com.viraj.dmabackend.lead.enums.LeadStatus;
import com.viraj.dmabackend.lead.exception.DuplicateLeadException;
import com.viraj.dmabackend.lead.exception.InvalidLeadStatusException;
import com.viraj.dmabackend.lead.exception.LeadAlreadyConvertedException;
import com.viraj.dmabackend.lead.repository.LeadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LeadValidator {

    private final LeadRepository leadRepository;
    private final UserRepository userRepository;

    public void validateDuplicateLead(CreateLeadRequest request) {
        if (leadRepository.existsByEmailAndStatusNot(request.getEmail(), LeadStatus.DELETED)) {
            throw new DuplicateLeadException("email", request.getEmail());
        }
        if (leadRepository.existsByPhoneNumberAndStatusNot(request.getPhoneNumber(), LeadStatus.DELETED)) {
            throw new DuplicateLeadException("phone number", request.getPhoneNumber());
        }
    }

    public void validateDuplicateLeadForUpdate(Lead existingLead, UpdateLeadRequest request) {
        leadRepository.findByEmailAndStatusNot(request.getEmail(), LeadStatus.DELETED)
                .ifPresent(lead -> {
                    if (!lead.getId().equals(existingLead.getId())) {
                        throw new DuplicateLeadException("email", request.getEmail());
                    }
                });

        leadRepository.findByPhoneNumberAndStatusNot(request.getPhoneNumber(), LeadStatus.DELETED)
                .ifPresent(lead -> {
                    if (!lead.getId().equals(existingLead.getId())) {
                        throw new DuplicateLeadException("phone number", request.getPhoneNumber());
                    }
                });
    }

    public void validateLeadStatus(LeadStatus currentStatus, LeadStatus newStatus) {
        if (currentStatus == newStatus) {
            return;
        }

        switch (currentStatus) {
            case NEW -> {
                if (newStatus != LeadStatus.CONTACTED && newStatus != LeadStatus.LOST) {
                    throw new InvalidLeadStatusException(currentStatus, newStatus);
                }
            }
            case CONTACTED -> {
                if (newStatus != LeadStatus.QUALIFIED && newStatus != LeadStatus.LOST) {
                    throw new InvalidLeadStatusException(currentStatus, newStatus);
                }
            }
            case QUALIFIED -> {
                if (newStatus != LeadStatus.PROPOSAL_SENT && newStatus != LeadStatus.LOST) {
                    throw new InvalidLeadStatusException(currentStatus, newStatus);
                }
            }
            case PROPOSAL_SENT -> {
                if (newStatus != LeadStatus.NEGOTIATION && newStatus != LeadStatus.LOST) {
                    throw new InvalidLeadStatusException(currentStatus, newStatus);
                }
            }
            case NEGOTIATION -> {
                if (newStatus != LeadStatus.WON && newStatus != LeadStatus.LOST) {
                    throw new InvalidLeadStatusException(currentStatus, newStatus);
                }
            }
            case WON, LOST -> throw new InvalidLeadStatusException(currentStatus, newStatus);
        }
    }

    public void validateLeadNotConverted(Lead lead) {
        if (lead.getConvertedClientId() != null) {
            throw new LeadAlreadyConvertedException(lead.getId());
        }
    }

    public void validateLeadConversion(Lead lead) {
        if (lead.getConvertedClientId() != null) {
            throw new LeadAlreadyConvertedException(lead.getId());
        }
        if (lead.getStatus() != LeadStatus.WON) {
            throw new InvalidLeadStatusException(lead.getStatus(), LeadStatus.WON);
        }
    }

    public void validateAssignedUser(String assignedUserId) {
        if (assignedUserId != null && !assignedUserId.isBlank()) {
            User user = userRepository.findByIdAndStatusNot(assignedUserId, UserStatus.DELETED)
                    .orElseThrow(() -> new UserNotFoundException(assignedUserId));
            if (user.getStatus() != UserStatus.ACTIVE) {
                throw new BadRequestException("Cannot assign lead to an inactive, suspended, or locked user.");
            }
        }
    }
}
