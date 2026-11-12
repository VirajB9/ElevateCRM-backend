package com.viraj.dmabackend.lead.validator;

import com.viraj.dmabackend.auth.dto.UserResponse;
import com.viraj.dmabackend.auth.enums.UserStatus;
import com.viraj.dmabackend.auth.exception.UserNotFoundException;
import com.viraj.dmabackend.auth.service.UserService;
import com.viraj.dmabackend.lead.dto.CreateLeadRequest;
import com.viraj.dmabackend.lead.dto.UpdateLeadRequest;
import com.viraj.dmabackend.lead.entity.Lead;
import com.viraj.dmabackend.lead.enums.LeadStatus;
import com.viraj.dmabackend.lead.exception.DuplicateLeadException;
import com.viraj.dmabackend.lead.exception.InvalidLeadStatusException;
import com.viraj.dmabackend.lead.exception.LeadAlreadyConvertedException;
import com.viraj.dmabackend.lead.repository.LeadRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LeadValidatorTest {

    @Mock
    private LeadRepository leadRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private LeadValidator leadValidator;

    @Test
    void validateLeadStatus_ValidTransitions_ShouldPass() {
        assertDoesNotThrow(() -> leadValidator.validateLeadStatus(LeadStatus.NEW, LeadStatus.CONTACTED));
        assertDoesNotThrow(() -> leadValidator.validateLeadStatus(LeadStatus.CONTACTED, LeadStatus.QUALIFIED));
        assertDoesNotThrow(() -> leadValidator.validateLeadStatus(LeadStatus.QUALIFIED, LeadStatus.PROPOSAL_SENT));
        assertDoesNotThrow(() -> leadValidator.validateLeadStatus(LeadStatus.PROPOSAL_SENT, LeadStatus.NEGOTIATION));
        assertDoesNotThrow(() -> leadValidator.validateLeadStatus(LeadStatus.NEGOTIATION, LeadStatus.WON));
    }

    @Test
    void validateLeadStatus_InvalidTransition_ShouldThrowException() {
        assertThrows(InvalidLeadStatusException.class, 
            () -> leadValidator.validateLeadStatus(LeadStatus.NEW, LeadStatus.WON));
    }

    @Test
    void validateDuplicateLead_ExistingEmail_ShouldThrowException() {
        CreateLeadRequest request = new CreateLeadRequest();
        request.setEmail("test@example.com");
        
        when(leadRepository.existsByEmailAndStatusNot("test@example.com", LeadStatus.DELETED)).thenReturn(true);

        assertThrows(DuplicateLeadException.class, 
            () -> leadValidator.validateDuplicateLead(request));
    }

    @Test
    void validateDuplicateLead_ExistingPhone_ShouldThrowException() {
        CreateLeadRequest request = new CreateLeadRequest();
        request.setEmail("new@example.com");
        request.setPhoneNumber("1234567890");
        
        when(leadRepository.existsByEmailAndStatusNot("new@example.com", LeadStatus.DELETED)).thenReturn(false);
        when(leadRepository.existsByPhoneNumberAndStatusNot("1234567890", LeadStatus.DELETED)).thenReturn(true);

        assertThrows(DuplicateLeadException.class, 
            () -> leadValidator.validateDuplicateLead(request));
    }

    @Test
    void validateLeadNotConverted_WhenConverted_ShouldThrowException() {
        Lead lead = new Lead();
        lead.setId("lead-123");
        lead.setConvertedClientId("client-456");

        assertThrows(LeadAlreadyConvertedException.class,
                () -> leadValidator.validateLeadNotConverted(lead));
    }

    @Test
    void validateLeadNotConverted_WhenNotConverted_ShouldPass() {
        Lead lead = new Lead();
        lead.setId("lead-123");
        lead.setConvertedClientId(null);

        assertDoesNotThrow(() -> leadValidator.validateLeadNotConverted(lead));
    }

    @Test
    void validateAssignedUser_NullOrBlank_ShouldPass() {
        assertDoesNotThrow(() -> leadValidator.validateAssignedUser(null));
        assertDoesNotThrow(() -> leadValidator.validateAssignedUser(""));
        assertDoesNotThrow(() -> leadValidator.validateAssignedUser("   "));
    }

    @Test
    void validateAssignedUser_ExistingActiveUser_ShouldPass() {
        UserResponse activeUser =
                UserResponse.builder().id("user-1").status(UserStatus.ACTIVE).build();
        when(userService.getUserById("user-1"))
                .thenReturn(activeUser);
        assertDoesNotThrow(() -> leadValidator.validateAssignedUser("user-1"));
    }

    @Test
    void validateAssignedUser_NonActiveUser_ShouldThrowBadRequest() {
        UserResponse inactiveUser =
                UserResponse.builder().id("user-2").status(UserStatus.SUSPENDED).build();
        when(userService.getUserById("user-2"))
                .thenReturn(inactiveUser);
        assertThrows(com.viraj.dmabackend.exception.BadRequestException.class,
                () -> leadValidator.validateAssignedUser("user-2"));
    }

    @Test
    void validateAssignedUser_NonExistentUser_ShouldThrow() {
        when(userService.getUserById("user-missing"))
                .thenThrow(new UserNotFoundException("user-missing"));
        assertThrows(UserNotFoundException.class, () -> leadValidator.validateAssignedUser("user-missing"));
    }

    @Test
    void validateDuplicateLead_NormalizesEmailToLowercaseAndTrim() {
        CreateLeadRequest request = new CreateLeadRequest();
        request.setEmail("  Lead.Test@EXAMPLE.COM  ");

        when(leadRepository.existsByEmailAndStatusNot("lead.test@example.com", LeadStatus.DELETED))
                .thenReturn(true);

        assertThrows(DuplicateLeadException.class, () -> leadValidator.validateDuplicateLead(request));
    }

    @Test
    void validateDuplicateLeadForUpdate_NormalizesEmailToLowercaseAndTrim() {
        Lead existing = Lead.builder().id("lead-1").build();
        Lead conflict = Lead.builder().id("lead-2").build();

        UpdateLeadRequest request = new UpdateLeadRequest();
        request.setEmail("  Other.Lead@EXAMPLE.COM  ");

        when(leadRepository.findByEmailAndStatusNot("other.lead@example.com", LeadStatus.DELETED))
                .thenReturn(Optional.of(conflict));

        assertThrows(DuplicateLeadException.class,
                () -> leadValidator.validateDuplicateLeadForUpdate(existing, request));
    }

    @Test
    void validateDuplicateLead_NormalizesPhoneWhitespace() {
        CreateLeadRequest request = new CreateLeadRequest();
        request.setPhoneNumber("  9876543210  ");

        when(leadRepository.existsByPhoneNumberAndStatusNot("9876543210", LeadStatus.DELETED))
                .thenReturn(true);

        assertThrows(DuplicateLeadException.class, () -> leadValidator.validateDuplicateLead(request));
    }

    @Test
    void validateDuplicateLeadForUpdate_NormalizesPhoneWhitespace() {
        Lead existing = Lead.builder().id("lead-1").build();
        Lead conflict = Lead.builder().id("lead-2").build();

        UpdateLeadRequest request = new UpdateLeadRequest();
        request.setPhoneNumber("  9876543210  ");

        when(leadRepository.findByPhoneNumberAndStatusNot("9876543210", LeadStatus.DELETED))
                .thenReturn(Optional.of(conflict));

        assertThrows(DuplicateLeadException.class,
                () -> leadValidator.validateDuplicateLeadForUpdate(existing, request));
    }

    @Test
    void validateUserExists_ExistingUser_ShouldPass() {
        when(userService.getUserById("user-1")).thenReturn(UserResponse.builder().id("user-1").build());
        assertDoesNotThrow(() -> leadValidator.validateUserExists("user-1"));
    }

    @Test
    void validateUserExists_NonExistingUser_ShouldThrowException() {
        when(userService.getUserById("user-missing")).thenThrow(new UserNotFoundException("user-missing"));
        assertThrows(UserNotFoundException.class, () -> leadValidator.validateUserExists("user-missing"));
    }

    @Test
    void validateAssignedUser_NormalizesWhitespace() {
        UserResponse user = UserResponse.builder()
                .id("user-123")
                .status(UserStatus.ACTIVE)
                .build();
        when(userService.getUserById("user-123"))
                .thenReturn(user);

        assertDoesNotThrow(() -> leadValidator.validateAssignedUser("   user-123   "));
        verify(userService).getUserById("user-123");
    }
}
