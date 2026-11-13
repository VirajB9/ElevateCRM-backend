package com.viraj.dmabackend.lead.service.impl;

import com.viraj.dmabackend.client.dto.ClientResponse;
import com.viraj.dmabackend.client.service.ClientService;
import com.viraj.dmabackend.lead.dto.CreateLeadRequest;
import com.viraj.dmabackend.lead.dto.UpdateLeadRequest;

import java.math.BigDecimal;
import com.viraj.dmabackend.lead.dto.LeadResponse;
import com.viraj.dmabackend.lead.entity.Lead;
import com.viraj.dmabackend.lead.enums.LeadSource;
import com.viraj.dmabackend.lead.enums.LeadStatus;
import com.viraj.dmabackend.lead.mapper.LeadMapper;
import com.viraj.dmabackend.lead.repository.LeadRepository;
import com.viraj.dmabackend.lead.validator.LeadValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LeadServiceImplTest {

    @Mock
    private LeadRepository leadRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private LeadMapper leadMapper;

    @Mock
    private LeadValidator leadValidator;

    @Mock
    private ClientService clientService;

    @InjectMocks
    private LeadServiceImpl leadService;

    private final Pageable pageable = PageRequest.of(0, 10);

    @Test
    void filterBySource_ExcludesDeletedLeads() {
        Lead lead = Lead.builder().id("lead-1").status(LeadStatus.NEW).source(LeadSource.LINKEDIN).build();
        when(leadRepository.findBySourceAndStatusNot(eq(LeadSource.LINKEDIN), eq(LeadStatus.DELETED), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(lead)));
        when(leadMapper.toLeadResponse(any(Lead.class)))
                .thenReturn(LeadResponse.builder().id("lead-1").status(LeadStatus.NEW).build());

        Page<LeadResponse> response = leadService.filterBySource(LeadSource.LINKEDIN, pageable);

        assertEquals(1, response.getTotalElements());
        verify(leadRepository).findBySourceAndStatusNot(eq(LeadSource.LINKEDIN), eq(LeadStatus.DELETED), eq(pageable));
    }

    @Test
    void filterByAssignedUser_ExcludesDeletedLeads() {
        Lead lead = Lead.builder().id("lead-2").assignedUserId("user-1").status(LeadStatus.CONTACTED).build();
        when(leadRepository.findByAssignedUserIdAndStatusNot(eq("user-1"), eq(LeadStatus.DELETED), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(lead)));
        when(leadMapper.toLeadResponse(any(Lead.class)))
                .thenReturn(LeadResponse.builder().id("lead-2").status(LeadStatus.CONTACTED).build());

        Page<LeadResponse> response = leadService.filterByAssignedUser("  user-1  ", pageable);

        assertEquals(1, response.getTotalElements());
        verify(leadValidator).validateUserExists("user-1");
        verify(leadRepository).findByAssignedUserIdAndStatusNot(eq("user-1"), eq(LeadStatus.DELETED), eq(pageable));
    }

    @Test
    void createLead_NormalizesStringsAndSetsBlankAssignedUserIdToNull() {
        com.viraj.dmabackend.lead.dto.CreateLeadRequest request = new com.viraj.dmabackend.lead.dto.CreateLeadRequest();
        request.setFirstName("  John  ");
        request.setLastName("  Doe  ");
        request.setCompanyName("  Acme Inc  ");
        request.setEmail("  John.Doe@EXAMPLE.COM  ");
        request.setPhoneNumber("  9876543210  ");
        request.setWebsite("  https://acme.com  ");
        request.setIndustry("  Tech  ");
        request.setSource(LeadSource.LINKEDIN);
        request.setAssignedUserId("   ");
        request.setRequirements("  Some requirements  ");
        request.setNotes("  Some notes  ");

        org.mockito.ArgumentCaptor<Lead> captor = org.mockito.ArgumentCaptor.forClass(Lead.class);
        when(leadRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));
        when(leadMapper.toLeadResponse(any(Lead.class))).thenReturn(LeadResponse.builder().id("lead-1").build());

        leadService.createLead(request);

        Lead savedLead = captor.getValue();
        assertEquals("John", savedLead.getFirstName());
        assertEquals("Doe", savedLead.getLastName());
        assertEquals("Acme Inc", savedLead.getCompanyName());
        assertEquals("john.doe@example.com", savedLead.getEmail());
        assertEquals("9876543210", savedLead.getPhoneNumber());
        assertEquals("https://acme.com", savedLead.getWebsite());
        assertEquals("Tech", savedLead.getIndustry());
        org.junit.jupiter.api.Assertions.assertNull(savedLead.getAssignedUserId());
        assertEquals("Some requirements", savedLead.getRequirements());
        assertEquals("Some notes", savedLead.getNotes());
    }

    @Test
    void updateLead_NormalizesStringsAndSetsBlankAssignedUserIdToNull() {
        Lead existingLead = Lead.builder()
                .id("lead-1")
                .status(LeadStatus.NEW)
                .assignedUserId("user-1")
                .build();

        when(leadRepository.findByIdAndStatusNot("lead-1", LeadStatus.DELETED)).thenReturn(java.util.Optional.of(existingLead));
        when(leadRepository.save(existingLead)).thenReturn(existingLead);
        when(leadMapper.toLeadResponse(existingLead)).thenReturn(LeadResponse.builder().id("lead-1").build());

        com.viraj.dmabackend.lead.dto.UpdateLeadRequest updateReq = new com.viraj.dmabackend.lead.dto.UpdateLeadRequest();
        updateReq.setFirstName("  Jane  ");
        updateReq.setLastName("  Smith  ");
        updateReq.setCompanyName("  Beta Corp  ");
        updateReq.setEmail("  Jane.Smith@BETA.COM  ");
        updateReq.setPhoneNumber("  9876543211  ");
        updateReq.setWebsite("  https://beta.com  ");
        updateReq.setIndustry("  Finance  ");
        updateReq.setSource(LeadSource.WEBSITE);
        updateReq.setStatus(LeadStatus.CONTACTED);
        updateReq.setAssignedUserId("");
        updateReq.setRequirements("  Updated req  ");
        updateReq.setNotes("  Updated notes  ");

        leadService.updateLead("lead-1", updateReq);

        assertEquals("Jane", existingLead.getFirstName());
        assertEquals("Smith", existingLead.getLastName());
        assertEquals("Beta Corp", existingLead.getCompanyName());
        assertEquals("jane.smith@beta.com", existingLead.getEmail());
        assertEquals("9876543211", existingLead.getPhoneNumber());
        assertEquals("https://beta.com", existingLead.getWebsite());
        assertEquals("Finance", existingLead.getIndustry());
        org.junit.jupiter.api.Assertions.assertNull(existingLead.getAssignedUserId());
        assertEquals("Updated req", existingLead.getRequirements());
        assertEquals("Updated notes", existingLead.getNotes());
    }

    @Test
    void convertLeadToClient_SetsConvertedAtWithUtcTimestamp() {
        Lead lead = Lead.builder()
                .id("lead-1")
                .firstName("Alice")
                .lastName("Smith")
                .companyName("Acme")
                .email("alice@acme.com")
                .status(LeadStatus.WON)
                .build();

        when(leadRepository.findByIdAndStatusNot("lead-1", LeadStatus.DELETED))
                .thenReturn(java.util.Optional.of(lead));

        ClientResponse client = ClientResponse.builder().id("client-123").build();
        when(clientService.createClientFromLead(any())).thenReturn(client);

        org.mockito.ArgumentCaptor<Lead> captor = org.mockito.ArgumentCaptor.forClass(Lead.class);
        when(leadRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));
        when(leadMapper.toLeadResponse(any(Lead.class)))
                .thenReturn(LeadResponse.builder().id("lead-1").convertedClientId("client-123").build());

        leadService.convertLeadToClient("lead-1");

        Lead saved = captor.getValue();
        assertEquals(LeadStatus.CONVERTED, saved.getStatus());
        assertEquals("client-123", saved.getConvertedClientId());
        org.junit.jupiter.api.Assertions.assertNotNull(saved.getConvertedAt());
        org.junit.jupiter.api.Assertions.assertTrue(saved.getConvertedAt().isAfter(java.time.LocalDateTime.now(java.time.ZoneOffset.UTC).minusMinutes(1)));
        org.junit.jupiter.api.Assertions.assertTrue(saved.getConvertedAt().isBefore(java.time.LocalDateTime.now(java.time.ZoneOffset.UTC).plusMinutes(1)));

        org.mockito.ArgumentCaptor<com.viraj.dmabackend.lead.event.LeadConvertedEvent> eventCaptor =
                org.mockito.ArgumentCaptor.forClass(com.viraj.dmabackend.lead.event.LeadConvertedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertEquals("client-123", eventCaptor.getValue().getClientId());
        assertEquals("lead-1", eventCaptor.getValue().getLeadId());
        assertEquals("client-123", eventCaptor.getValue().getData().getClientId());
    }

    @Test
    void createLead_NormalizesEstimatedBudgetToTwoDecimalPlaces() {
        CreateLeadRequest request = new CreateLeadRequest();
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setCompanyName("Acme");
        request.setEmail("jane@example.com");
        request.setEstimatedBudget(new BigDecimal("5000.789"));

        org.mockito.ArgumentCaptor<Lead> captor = org.mockito.ArgumentCaptor.forClass(Lead.class);
        when(leadRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));
        when(leadMapper.toLeadResponse(any(Lead.class))).thenReturn(LeadResponse.builder().id("lead-1").build());

        leadService.createLead(request);

        Lead saved = captor.getValue();
        assertEquals(new BigDecimal("5000.79"), saved.getEstimatedBudget());
    }

    @Test
    void getLeadById_TrimsLeadId() {
        Lead lead = Lead.builder().id("lead-1").build();
        when(leadRepository.findByIdAndStatusNot("lead-1", LeadStatus.DELETED))
                .thenReturn(java.util.Optional.of(lead));
        when(leadMapper.toLeadResponse(lead))
                .thenReturn(LeadResponse.builder().id("lead-1").build());

        LeadResponse response = leadService.getLeadById("   lead-1   ");

        org.junit.jupiter.api.Assertions.assertNotNull(response);
        verify(leadRepository).findByIdAndStatusNot("lead-1", LeadStatus.DELETED);
    }

    @Test
    void convertLeadToClient_whenLeadSaveFails_purgesOrphanedClient() {
        Lead lead = Lead.builder()
                .id("lead-1")
                .status(LeadStatus.WON)
                .email("test@lead.com")
                .phoneNumber("+1234567890")
                .build();

        when(leadRepository.findByIdAndStatusNot("lead-1", LeadStatus.DELETED))
                .thenReturn(java.util.Optional.of(lead));
        when(clientService.createClientFromLead(any()))
                .thenReturn(ClientResponse.builder().id("orphaned-client-1").build());
        when(leadRepository.save(any(Lead.class)))
                .thenThrow(new RuntimeException("Database timeout on lead save"))
                .thenAnswer(invocation -> invocation.getArgument(0));

        org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> leadService.convertLeadToClient("lead-1")
        );

        verify(clientService).purgeOrphanedClient("orphaned-client-1");
        assertEquals(LeadStatus.WON, lead.getStatus());
        org.junit.jupiter.api.Assertions.assertNull(lead.getConvertedClientId());
    }

    @Test
    void convertLeadToClient_whenEventPublishThrowsException_doesNotCorruptLeadOrPurgeClient() {
        Lead lead = Lead.builder()
                .id("lead-1")
                .status(LeadStatus.WON)
                .email("test@lead.com")
                .phoneNumber("+1234567890")
                .build();

        when(leadRepository.findByIdAndStatusNot("lead-1", LeadStatus.DELETED))
                .thenReturn(java.util.Optional.of(lead));
        when(clientService.createClientFromLead(any()))
                .thenReturn(ClientResponse.builder().id("client-123").build());
        when(leadRepository.save(any(Lead.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(leadMapper.toLeadResponse(any(Lead.class)))
                .thenReturn(LeadResponse.builder().id("lead-1").convertedClientId("client-123").build());
        doThrow(new RuntimeException("Downstream webhook failed"))
                .when(eventPublisher).publishEvent(any());

        LeadResponse response = leadService.convertLeadToClient("lead-1");

        org.junit.jupiter.api.Assertions.assertNotNull(response);
        assertEquals(LeadStatus.CONVERTED, lead.getStatus());
        assertEquals("client-123", lead.getConvertedClientId());
        verify(clientService, never()).purgeOrphanedClient(any());
    }

    @Test
    void filterByAssignedUser_callsValidation() {
        when(leadRepository.findByAssignedUserIdAndStatusNot(eq("user-1"), eq(LeadStatus.DELETED), any()))
                .thenReturn(Page.empty());

        leadService.filterByAssignedUser("user-1", PageRequest.of(0, 10));

        verify(leadValidator).validateUserExists("user-1");
        verify(leadValidator).validateAssignedUserAccess("user-1");
    }

    @Test
    void updateLead_ThrowsOptimisticLockingFailure_WhenVersionMismatch() {
        Lead lead = Lead.builder().id("lead-1").build();
        lead.setVersion(2L);
        when(leadRepository.findByIdAndStatusNot("lead-1", LeadStatus.DELETED))
                .thenReturn(java.util.Optional.of(lead));

        UpdateLeadRequest request = new UpdateLeadRequest();
        request.setVersion(1L);

        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.dao.OptimisticLockingFailureException.class,
                () -> leadService.updateLead("lead-1", request)
        );
    }

    @Test
    void getLeadById_ValidatesLeadAccess() {
        Lead lead = Lead.builder().id("lead-1").assignedUserId("other-user").build();
        when(leadRepository.findByIdAndStatusNot("lead-1", LeadStatus.DELETED))
                .thenReturn(java.util.Optional.of(lead));
        doThrow(new org.springframework.security.access.AccessDeniedException("Access denied"))
                .when(leadValidator).validateLeadAccess(lead);

        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.security.access.AccessDeniedException.class,
                () -> leadService.getLeadById("lead-1")
        );

        verify(leadValidator).validateLeadAccess(lead);
    }

    @Test
    void getAllLeads_WhenRestrictedScope_FiltersByAssignedUser() {
        when(leadValidator.getAssignedUserScopeIfRestricted()).thenReturn("emp-1");
        when(leadRepository.findByAssignedUserIdAndStatusNot("emp-1", LeadStatus.DELETED, pageable))
                .thenReturn(Page.empty());

        leadService.getAllLeads(pageable);

        verify(leadRepository).findByAssignedUserIdAndStatusNot("emp-1", LeadStatus.DELETED, pageable);
        verify(leadRepository, never()).findByStatusNot(LeadStatus.DELETED, pageable);
    }

    @Test
    void searchLeads_WhenRestrictedScope_CallsSearchWithAssignedUserId() {
        when(leadValidator.getAssignedUserScopeIfRestricted()).thenReturn("emp-1");
        when(leadRepository.searchLeads("test", "emp-1", pageable))
                .thenReturn(Page.empty());

        leadService.searchLeads("test", pageable);

        verify(leadRepository).searchLeads("test", "emp-1", pageable);
        verify(leadRepository, never()).searchLeads("test", pageable);
    }

    @Test
    void filterByStatus_WhenRestrictedScope_CallsFindByStatusAndAssignedUserId() {
        when(leadValidator.getAssignedUserScopeIfRestricted()).thenReturn("emp-1");
        when(leadRepository.findByStatusAndAssignedUserId(LeadStatus.NEW, "emp-1", pageable))
                .thenReturn(Page.empty());

        leadService.filterByStatus(LeadStatus.NEW, pageable);

        verify(leadRepository).findByStatusAndAssignedUserId(LeadStatus.NEW, "emp-1", pageable);
        verify(leadRepository, never()).findByStatus(LeadStatus.NEW, pageable);
    }

    @Test
    void filterBySource_WhenRestrictedScope_CallsFindBySourceAndAssignedUserId() {
        when(leadValidator.getAssignedUserScopeIfRestricted()).thenReturn("emp-1");
        when(leadRepository.findBySourceAndAssignedUserIdAndStatusNot(LeadSource.LINKEDIN, "emp-1", LeadStatus.DELETED, pageable))
                .thenReturn(Page.empty());

        leadService.filterBySource(LeadSource.LINKEDIN, pageable);

        verify(leadRepository).findBySourceAndAssignedUserIdAndStatusNot(LeadSource.LINKEDIN, "emp-1", LeadStatus.DELETED, pageable);
        verify(leadRepository, never()).findBySourceAndStatusNot(LeadSource.LINKEDIN, LeadStatus.DELETED, pageable);
    }
}
