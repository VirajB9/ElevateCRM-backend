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
        if (userRepository.existsByEmailAndStatusNot(email, UserStatus.DELETED)) {
            throw new DuplicateEmailException(email);
        }
    }

    public void validateDuplicatePhone(String phoneNumber) {
        if (userRepository.existsByPhoneNumberAndStatusNot(phoneNumber, UserStatus.DELETED)) {
            throw new DuplicatePhoneException(phoneNumber);
        }
    }

    public void validatePhoneForUpdate(String phoneNumber, String userId) {
        if (userRepository.existsByPhoneNumberAndIdNotAndStatusNot(phoneNumber, userId, UserStatus.DELETED)) {
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
            if ("MANAGER".equals(targetUserRole.getName())) {
                throw new UnauthorizedRoleAssignmentException("Managers cannot modify or delete other Managers");
            }
            if (!"EMPLOYEE".equals(targetUserRole.getName()) && !"INTERN".equals(targetUserRole.getName())) {
                throw new UnauthorizedRoleAssignmentException("Managers can only manage Employees or Interns");
            }
            return;
        }

        throw new UnauthorizedRoleAssignmentException("You are not authorized to manage users");
    }
}
