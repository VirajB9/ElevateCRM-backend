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
}
