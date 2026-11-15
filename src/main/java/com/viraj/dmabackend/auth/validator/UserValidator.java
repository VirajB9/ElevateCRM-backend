package com.viraj.dmabackend.auth.validator;

import com.viraj.dmabackend.auth.entity.Role;
import com.viraj.dmabackend.auth.entity.User;
import com.viraj.dmabackend.auth.enums.UserStatus;
import com.viraj.dmabackend.auth.exception.DuplicateEmailException;
import com.viraj.dmabackend.auth.exception.DuplicatePhoneException;
import com.viraj.dmabackend.auth.exception.InvalidUserStatusException;
import com.viraj.dmabackend.auth.exception.UnauthorizedRoleAssignmentException;
import com.viraj.dmabackend.auth.repository.UserRepository;
import com.viraj.dmabackend.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserValidator {

    private final UserRepository userRepository;
    private final List<UserDeletionGuard> userDeletionGuards;

    public void validateDuplicateEmail(String email) {
        String normalizedEmail = email != null ? email.trim().toLowerCase() : null;
        if (normalizedEmail != null && userRepository.existsByEmailAndStatusNot(normalizedEmail, UserStatus.DELETED)) {
            throw new DuplicateEmailException(normalizedEmail);
        }
    }

    public void validateDuplicatePhone(String phoneNumber) {
        String normalizedPhone = phoneNumber != null ? phoneNumber.trim() : null;
        if (normalizedPhone != null && userRepository.existsByPhoneNumberAndStatusNot(normalizedPhone, UserStatus.DELETED)) {
            throw new DuplicatePhoneException(phoneNumber);
        }
    }

    public void validatePhoneForUpdate(String phoneNumber, String userId) {
        String normalizedPhone = phoneNumber != null ? phoneNumber.trim() : null;
        if (normalizedPhone != null && userRepository.existsByPhoneNumberAndIdNotAndStatusNot(normalizedPhone, userId, UserStatus.DELETED)) {
            throw new DuplicatePhoneException(phoneNumber);
        }
    }

    public void validateStatusUpdate(UserStatus status, String userId) {
        if (status == UserStatus.DELETED) {
            throw new InvalidUserStatusException(status);
        }

        String normalizedUserId = userId != null ? userId.trim() : null;
        if ((status == UserStatus.INACTIVE || status == UserStatus.SUSPENDED || status == UserStatus.LOCKED) && normalizedUserId != null) {
            if (userDeletionGuards != null) {
                for (UserDeletionGuard guard : userDeletionGuards) {
                    guard.validateCanDeactivateUser(normalizedUserId);
                }
            }
        }
    }

    public void validateStatusUpdate(UserStatus status) {
        validateStatusUpdate(status, null);
    }

    public void validateRoleAssignment(User currentUser, Role currentUserRole, Role targetRole) {
        String currentRole = currentUserRole.getName();
        String targetRoleName = targetRole.getName();

        if ("OWNER".equals(currentRole)) {
            if ("OWNER".equals(targetRoleName)) {
                throw new UnauthorizedRoleAssignmentException("Cannot assign the system OWNER role to new users");
            }
            return;
        }

        throw new UnauthorizedRoleAssignmentException("You are not allowed to create users");
    }

    public void validateUserManagement(User currentUser, Role currentUserRole, User targetUser, Role targetUserRole) {
        if ("OWNER".equals(currentUserRole.getName())) {
            return;
        }

        if ("OWNER".equals(targetUserRole.getName())) {
            throw new UnauthorizedRoleAssignmentException("Non-owner users cannot manage the Owner account");
        }

        throw new UnauthorizedRoleAssignmentException("You are not authorized to manage users");
    }

    public void validateUserDeletable(String userId) {
        if (userDeletionGuards != null) {
            for (UserDeletionGuard guard : userDeletionGuards) {
                guard.validateCanDeleteUser(userId);
            }
        }
    }
}
