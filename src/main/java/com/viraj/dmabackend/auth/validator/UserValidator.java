package com.viraj.dmabackend.auth.validator;

import com.viraj.dmabackend.auth.entity.Role;
import com.viraj.dmabackend.auth.entity.User;
import com.viraj.dmabackend.auth.enums.UserStatus;
import com.viraj.dmabackend.auth.exception.DuplicateEmailException;
import com.viraj.dmabackend.auth.exception.DuplicatePhoneException;
import com.viraj.dmabackend.auth.exception.InvalidUserStatusException;
import com.viraj.dmabackend.auth.exception.UnauthorizedRoleAssignmentException;
import com.viraj.dmabackend.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserValidator {

    private final UserRepository userRepository;

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

    public void validateStatusUpdate(UserStatus status) {
        if (status == UserStatus.DELETED) {
            throw new InvalidUserStatusException(status);
        }
    }

    public void validateRoleAssignment(User currentUser, Role currentUserRole, Role targetRole) {
        String currentRole = currentUserRole.getName();
        String targetRoleName = targetRole.getName();

        switch (currentRole) {
            case "OWNER":
                if ("OWNER".equals(targetRoleName)) {
                    throw new UnauthorizedRoleAssignmentException("Cannot assign the system OWNER role to new users");
                }
                return;

            case "MANAGER":
                if (!targetRoleName.equals("EMPLOYEE") && !targetRoleName.equals("INTERN")) {
                    throw new UnauthorizedRoleAssignmentException("Managers can only create Employees or Interns");
                }
                return;

            case "EMPLOYEE":
            case "INTERN":
                throw new UnauthorizedRoleAssignmentException("You are not allowed to create users");

            default:
                throw new UnauthorizedRoleAssignmentException("Invalid role assignment");
        }
    }

    public void validateUserManagement(User currentUser, Role currentUserRole, User targetUser, Role targetUserRole) {
        if ("OWNER".equals(currentUserRole.getName())) {
            return;
        }

        if ("OWNER".equals(targetUserRole.getName())) {
            throw new UnauthorizedRoleAssignmentException("Non-owner users cannot manage the Owner account");
        }

        if ("MANAGER".equals(currentUserRole.getName())) {
            boolean isSelf = currentUser.getId() != null && currentUser.getId().equals(targetUser.getId());
            if ("MANAGER".equals(targetUserRole.getName()) && !isSelf) {
                throw new UnauthorizedRoleAssignmentException("Managers cannot modify or delete other Managers");
            }
            if (!isSelf
                    && !"EMPLOYEE".equals(targetUserRole.getName())
                    && !"INTERN".equals(targetUserRole.getName())) {
                throw new UnauthorizedRoleAssignmentException("Managers can only manage Employees or Interns");
            }
            return;
        }

        throw new UnauthorizedRoleAssignmentException("You are not authorized to manage users");
    }
}
