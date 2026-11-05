package com.viraj.dmabackend.lead.service.impl;

import com.viraj.dmabackend.client.service.ClientService;
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

        Page<LeadResponse> response = leadService.filterByAssignedUser("user-1", pageable);

        assertEquals(1, response.getTotalElements());
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
}
