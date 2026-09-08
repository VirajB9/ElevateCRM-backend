package com.viraj.dmabackend.auth.validator;

import com.viraj.dmabackend.auth.entity.Role;
import com.viraj.dmabackend.auth.entity.User;
import com.viraj.dmabackend.auth.exception.UnauthorizedRoleAssignmentException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class UserValidatorTest {

    @InjectMocks
    private UserValidator userValidator;

    @Test
    void validateRoleAssignment_OwnerCanCreateAnyRole() {
        User ownerUser = new User();
        Role ownerRole = new Role();
        ownerRole.setName("OWNER");
        
        Role targetRole = new Role();
        targetRole.setName("MANAGER");
        
        assertDoesNotThrow(() -> userValidator.validateRoleAssignment(ownerUser, ownerRole, targetRole));
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
}
