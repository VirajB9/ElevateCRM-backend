package com.viraj.dmabackend.lead.validator;

import com.viraj.dmabackend.lead.dto.CreateLeadRequest;
import com.viraj.dmabackend.lead.dto.UpdateLeadRequest;
import com.viraj.dmabackend.lead.entity.Lead;
import com.viraj.dmabackend.lead.enums.LeadStatus;
import com.viraj.dmabackend.lead.exception.DuplicateLeadException;
import com.viraj.dmabackend.lead.exception.InvalidLeadStatusException;
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
        
        when(leadRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(DuplicateLeadException.class, 
            () -> leadValidator.validateDuplicateLead(request));
    }

    @Test
    void validateDuplicateLead_ExistingPhone_ShouldThrowException() {
        CreateLeadRequest request = new CreateLeadRequest();
        request.setEmail("new@example.com");
        request.setPhoneNumber("1234567890");
        
        when(leadRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(leadRepository.existsByPhoneNumber("1234567890")).thenReturn(true);

        assertThrows(DuplicateLeadException.class, 
            () -> leadValidator.validateDuplicateLead(request));
    }
}
