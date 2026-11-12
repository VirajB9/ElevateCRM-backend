package com.viraj.dmabackend.lead.validator;

import com.viraj.dmabackend.auth.dto.UserResponse;
import com.viraj.dmabackend.auth.enums.UserStatus;
import com.viraj.dmabackend.auth.exception.UserNotFoundException;
import com.viraj.dmabackend.auth.service.UserService;
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
    private final UserService userService;

    public void validateDuplicateLead(CreateLeadRequest request) {
        String email = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : null;
        if (email != null && leadRepository.existsByEmailAndStatusNot(email, LeadStatus.DELETED)) {
            throw new DuplicateLeadException("email", request.getEmail());
        }
        String phone = request.getPhoneNumber() != null ? request.getPhoneNumber().trim() : null;
        if (phone != null && leadRepository.existsByPhoneNumberAndStatusNot(phone, LeadStatus.DELETED)) {
            throw new DuplicateLeadException("phone number", request.getPhoneNumber());
        }
    }

    public void validateDuplicateLeadForUpdate(Lead existingLead, UpdateLeadRequest request) {
        String email = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : null;
        if (email != null) {
            leadRepository.findByEmailAndStatusNot(email, LeadStatus.DELETED)
                    .ifPresent(lead -> {
                        if (!lead.getId().equals(existingLead.getId())) {
                            throw new DuplicateLeadException("email", request.getEmail());
                        }
                    });
        }

        String phone = request.getPhoneNumber() != null ? request.getPhoneNumber().trim() : null;
        if (phone != null) {
            leadRepository.findByPhoneNumberAndStatusNot(phone, LeadStatus.DELETED)
                    .ifPresent(lead -> {
                        if (!lead.getId().equals(existingLead.getId())) {
                            throw new DuplicateLeadException("phone number", request.getPhoneNumber());
                        }
                    });
        }
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
            case WON, LOST, CONVERTED -> throw new InvalidLeadStatusException(currentStatus, newStatus);
        }
    }

    public void validateLeadNotConverted(Lead lead) {
        if (lead.getConvertedClientId() != null || lead.getStatus() == LeadStatus.CONVERTED) {
            throw new LeadAlreadyConvertedException(lead.getId());
        }
    }

    public void validateLeadConversion(Lead lead) {
        if (lead.getConvertedClientId() != null || lead.getStatus() == LeadStatus.CONVERTED) {
            throw new LeadAlreadyConvertedException(lead.getId());
        }
        if (lead.getStatus() != LeadStatus.WON) {
            throw new InvalidLeadStatusException(lead.getStatus(), LeadStatus.WON);
        }
    }

    public void validateAssignedUser(String assignedUserId) {
        if (assignedUserId != null && !assignedUserId.isBlank()) {
            String normalizedUserId = assignedUserId.trim();
            UserResponse user = userService.getUserById(normalizedUserId);
            if (user.getStatus() != UserStatus.ACTIVE) {
                throw new BadRequestException("Cannot assign lead to an inactive, suspended, or locked user.");
            }
        }
    }

    public void validateUserExists(String userId) {
        String normalizedUserId = userId != null ? userId.trim() : null;
        if (normalizedUserId == null || normalizedUserId.isBlank()) {
            throw new UserNotFoundException(userId);
        }
        userService.getUserById(normalizedUserId);
    }
}
