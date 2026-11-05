package com.viraj.dmabackend.lead.controller;

import com.viraj.dmabackend.lead.dto.LeadResponse;
import com.viraj.dmabackend.lead.enums.LeadStatus;
import com.viraj.dmabackend.lead.service.LeadService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LeadControllerTest {

    @Mock
    private LeadService leadService;

    @InjectMocks
    private LeadController leadController;

    @Test
    void filterByStatus_DelegatesToService() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<LeadResponse> mockPage = new PageImpl<>(List.of(LeadResponse.builder().id("lead-1").status(LeadStatus.QUALIFIED).build()));

        when(leadService.filterByStatus(LeadStatus.QUALIFIED, pageable)).thenReturn(mockPage);

        Page<LeadResponse> result = leadController.filterByStatus(LeadStatus.QUALIFIED, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("lead-1", result.getContent().get(0).getId());
        verify(leadService).filterByStatus(LeadStatus.QUALIFIED, pageable);
    }
}
