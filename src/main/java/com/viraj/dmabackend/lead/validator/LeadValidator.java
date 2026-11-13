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

    public static final java.util.Set<LeadStatus> INACTIVE_LEAD_STATUSES = java.util.Set.of(
            LeadStatus.DELETED,
            LeadStatus.LOST,
            LeadStatus.CONVERTED
    );

    private final LeadRepository leadRepository;
    private final UserService userService;

    public void validateDuplicateLead(CreateLeadRequest request) {
        String email = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : null;
        if (email != null && leadRepository.existsByEmailAndStatusNotIn(email, INACTIVE_LEAD_STATUSES)) {
            throw new DuplicateLeadException("email", request.getEmail());
        }
        String phone = request.getPhoneNumber() != null ? request.getPhoneNumber().trim() : null;
        if (phone != null && leadRepository.existsByPhoneNumberAndStatusNotIn(phone, INACTIVE_LEAD_STATUSES)) {
            throw new DuplicateLeadException("phone number", request.getPhoneNumber());
        }
    }

    public void validateDuplicateLeadForUpdate(Lead existingLead, UpdateLeadRequest request) {
        String email = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : null;
        if (email != null) {
            leadRepository.findByEmailAndStatusNotIn(email, INACTIVE_LEAD_STATUSES)
                    .ifPresent(lead -> {
                        if (!lead.getId().equals(existingLead.getId())) {
                            throw new DuplicateLeadException("email", request.getEmail());
                        }
                    });
        }

        String phone = request.getPhoneNumber() != null ? request.getPhoneNumber().trim() : null;
        if (phone != null) {
            leadRepository.findByPhoneNumberAndStatusNotIn(phone, INACTIVE_LEAD_STATUSES)
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
            case WON -> {
                if (newStatus != LeadStatus.LOST) {
                    throw new InvalidLeadStatusException(currentStatus, newStatus);
                }
            }
            case LOST -> {
                if (newStatus != LeadStatus.NEW && newStatus != LeadStatus.CONTACTED) {
                    throw new InvalidLeadStatusException(currentStatus, newStatus);
                }
            }
            case CONVERTED, DELETED -> throw new InvalidLeadStatusException(currentStatus, newStatus);
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

    public String getAssignedUserScopeIfRestricted() {
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) {
            return null;
        }

        boolean isManagerOrOwner = auth.getAuthorities().stream().anyMatch(a ->
                "ROLE_OWNER".equals(a.getAuthority()) || "ROLE_MANAGER".equals(a.getAuthority()) || "ROLE_ADMIN".equals(a.getAuthority()));
        if (isManagerOrOwner) {
            return null;
        }

        String currentUserEmail = auth.getName();
        if (currentUserEmail != null && !currentUserEmail.isBlank()) {
            try {
                UserResponse currentUser = userService.getUserByEmail(currentUserEmail);
                if (currentUser != null) {
                    return currentUser.getId();
                }
            } catch (UserNotFoundException ignored) {
            }
        }
        return null;
    }

    public void validateLeadAccess(Lead lead) {
        String restrictedUserId = getAssignedUserScopeIfRestricted();
        if (restrictedUserId != null && (lead.getAssignedUserId() == null || !restrictedUserId.equals(lead.getAssignedUserId()))) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Access denied: You are not authorized to view leads assigned to another user");
        }
    }

    public void validateAssignedUserAccess(String userId) {
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) {
            return;
        }

        boolean isManagerOrOwner = auth.getAuthorities().stream().anyMatch(a ->
                "ROLE_OWNER".equals(a.getAuthority()) || "ROLE_MANAGER".equals(a.getAuthority()));
        if (isManagerOrOwner) {
            return;
        }

        String currentUserEmail = auth.getName();
        if (currentUserEmail != null && !currentUserEmail.isBlank()) {
            try {
                UserResponse currentUser = userService.getUserByEmail(currentUserEmail);
                if (currentUser != null && !currentUser.getId().equals(userId)) {
                    throw new org.springframework.security.access.AccessDeniedException(
                            "Access denied: You are not authorized to view leads assigned to another user");
                }
            } catch (UserNotFoundException ignored) {
                // If user not in DB, continue
            }
        }
    }
}
