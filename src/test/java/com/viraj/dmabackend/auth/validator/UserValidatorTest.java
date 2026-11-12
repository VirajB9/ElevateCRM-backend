package com.viraj.dmabackend.auth.validator;

import com.viraj.dmabackend.auth.entity.Role;
import com.viraj.dmabackend.auth.entity.User;
import com.viraj.dmabackend.auth.exception.UnauthorizedRoleAssignmentException;
import com.viraj.dmabackend.auth.exception.DuplicateEmailException;
import com.viraj.dmabackend.auth.exception.DuplicatePhoneException;
import com.viraj.dmabackend.auth.repository.UserRepository;
import com.viraj.dmabackend.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserValidatorTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDeletionGuard userDeletionGuard;

    private UserValidator userValidator;

    @BeforeEach
    void setUp() {
        userValidator = new UserValidator(userRepository, List.of(userDeletionGuard));
    }

    @Test
    void validateRoleAssignment_OwnerCanCreateManagerRole() {
        User ownerUser = new User();
        Role ownerRole = new Role();
        ownerRole.setName("OWNER");
        
        Role targetRole = new Role();
        targetRole.setName("MANAGER");
        
        assertDoesNotThrow(() -> userValidator.validateRoleAssignment(ownerUser, ownerRole, targetRole));
    }

    @Test
    void validateRoleAssignment_OwnerCannotAssignOwnerRole() {
        User ownerUser = new User();
        Role ownerRole = new Role();
        ownerRole.setName("OWNER");

        Role targetRole = new Role();
        targetRole.setName("OWNER");

        assertThrows(UnauthorizedRoleAssignmentException.class,
                () -> userValidator.validateRoleAssignment(ownerUser, ownerRole, targetRole));
    }

    @Test
    void validateRoleAssignment_ManagerCanCreateEmployee() {
        User managerUser = new User();
        Role managerRole = new Role();
        managerRole.setName("MANAGER");
        
        Role targetRole = new Role();
        targetRole.setName("EMPLOYEE");
        
        assertDoesNotThrow(() -> userValidator.validateRoleAssignment(managerUser, managerRole, targetRole));
    }

    @Test
    void validateRoleAssignment_ManagerCannotCreateOwner() {
        User managerUser = new User();
        Role managerRole = new Role();
        managerRole.setName("MANAGER");
        
        Role targetRole = new Role();
        targetRole.setName("OWNER");
        
        assertThrows(UnauthorizedRoleAssignmentException.class, 
            () -> userValidator.validateRoleAssignment(managerUser, managerRole, targetRole));
    }

    @Test
    void validateRoleAssignment_EmployeeCannotCreateUsers() {
        User employeeUser = new User();
        Role employeeRole = new Role();
        employeeRole.setName("EMPLOYEE");
        
        Role targetRole = new Role();
        targetRole.setName("INTERN");
        
        assertThrows(UnauthorizedRoleAssignmentException.class, 
            () -> userValidator.validateRoleAssignment(employeeUser, employeeRole, targetRole));
    }

    @Test
    void validateUserManagement_OwnerCanManageAnyone() {
        Role ownerRole = Role.builder().name("OWNER").build();
        Role managerRole = Role.builder().name("MANAGER").build();

        assertDoesNotThrow(() -> userValidator.validateUserManagement(
                new User(), ownerRole, new User(), managerRole));
    }

    @Test
    void validateUserManagement_ManagerCannotManageOtherManagers() {
        Role managerRole = Role.builder().name("MANAGER").build();

        assertThrows(UnauthorizedRoleAssignmentException.class, () ->
                userValidator.validateUserManagement(new User(), managerRole, new User(), managerRole));
    }

    @Test
    void validateUserManagement_ManagerCannotManageOwner() {
        Role managerRole = Role.builder().name("MANAGER").build();
        Role ownerRole = Role.builder().name("OWNER").build();

        assertThrows(UnauthorizedRoleAssignmentException.class, () ->
                userValidator.validateUserManagement(new User(), managerRole, new User(), ownerRole));
    }

    @Test
    void validateUserManagement_ManagerCanManageEmployee() {
        Role managerRole = Role.builder().name("MANAGER").build();
        Role employeeRole = Role.builder().name("EMPLOYEE").build();

        assertDoesNotThrow(() ->
                userValidator.validateUserManagement(new User(), managerRole, new User(), employeeRole));
    }

    @Test
    void validateUserManagement_ManagerCanManageSelf() {
        Role managerRole = Role.builder().name("MANAGER").build();
        User manager = User.builder().id("m-1").build();

        assertDoesNotThrow(() ->
                userValidator.validateUserManagement(manager, managerRole, manager, managerRole));
    }

    @Test
    void validateDuplicateEmail_NormalizesCaseAndWhitespace() {
        when(userRepository.existsByEmailAndStatusNot("john@example.com", com.viraj.dmabackend.auth.enums.UserStatus.DELETED))
                .thenReturn(true);

        assertThrows(DuplicateEmailException.class,
                () -> userValidator.validateDuplicateEmail("  John@Example.com  "));
    }

    @Test
    void validateDuplicatePhone_NormalizesWhitespace() {
        when(userRepository.existsByPhoneNumberAndStatusNot("9876543210", com.viraj.dmabackend.auth.enums.UserStatus.DELETED))
                .thenReturn(true);

        assertThrows(DuplicatePhoneException.class,
                () -> userValidator.validateDuplicatePhone("  9876543210  "));
    }

    @Test
    void validatePhoneForUpdate_NormalizesWhitespace() {
        when(userRepository.existsByPhoneNumberAndIdNotAndStatusNot("9876543210", "u-1", com.viraj.dmabackend.auth.enums.UserStatus.DELETED))
                .thenReturn(true);

        assertThrows(DuplicatePhoneException.class,
                () -> userValidator.validatePhoneForUpdate("  9876543210  ", "u-1"));
    }

    @Test
    void validateUserDeletable_ThrowsWhenActiveAssignedLeadsExist() {
        doThrow(new BadRequestException("Cannot delete user with active assigned leads. Please reassign the leads first."))
                .when(userDeletionGuard).validateCanDeleteUser("  u-1  ");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> userValidator.validateUserDeletable("  u-1  "));
        assertTrue(ex.getMessage().contains("active assigned leads"));
    }

    @Test
    void validateUserDeletable_PassesWhenNoActiveAssignedLeads() {
        doNothing().when(userDeletionGuard).validateCanDeleteUser("u-1");

        assertDoesNotThrow(() -> userValidator.validateUserDeletable("u-1"));
    }
}
