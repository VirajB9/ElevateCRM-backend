package com.viraj.dmabackend.lead.controller;

import com.viraj.dmabackend.controller.BaseControllerWebMvcTest;
import com.viraj.dmabackend.lead.dto.CreateLeadRequest;
import com.viraj.dmabackend.lead.dto.LeadResponse;
import com.viraj.dmabackend.lead.dto.UpdateLeadRequest;
import com.viraj.dmabackend.lead.enums.LeadSource;
import com.viraj.dmabackend.lead.enums.LeadStatus;
import com.viraj.dmabackend.lead.service.LeadService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LeadController.class)
class LeadControllerTest extends BaseControllerWebMvcTest {

    @MockBean
    private LeadService leadService;

    @Test
    @WithMockUser(authorities = "lead:create")
    void createLead_whenAuthorized_returnsCreated() throws Exception {
        CreateLeadRequest request = new CreateLeadRequest();
        request.setFirstName("Peter");
        request.setLastName("Parker");
        request.setCompanyName("Daily Bugle");
        request.setEmail("peter@example.com");
        request.setPhoneNumber("9876543210");
        request.setSource(LeadSource.WEBSITE);

        when(leadService.createLead(any())).thenReturn(LeadResponse.builder().id("lead-1").build());

        mockMvc.perform(post("/api/v1/leads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(authorities = "lead:read")
    void createLead_whenInsufficientAuthority_returnsForbidden() throws Exception {
        CreateLeadRequest request = new CreateLeadRequest();
        request.setFirstName("Peter");
        request.setLastName("Parker");
        request.setCompanyName("Daily Bugle");
        request.setEmail("peter@example.com");
        request.setPhoneNumber("9876543210");
        request.setSource(LeadSource.WEBSITE);

        mockMvc.perform(post("/api/v1/leads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createLead_whenUnauthenticated_returnsUnauthorized() throws Exception {
        CreateLeadRequest request = new CreateLeadRequest();
        request.setFirstName("Peter");
        request.setLastName("Parker");

        mockMvc.perform(post("/api/v1/leads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "lead:read")
    void getLeadById_whenAuthorized_returnsOk() throws Exception {
        when(leadService.getLeadById("lead-1")).thenReturn(LeadResponse.builder().id("lead-1").build());

        mockMvc.perform(get("/api/v1/leads/lead-1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "lead:read")
    void getAllLeads_whenAuthorized_returnsOk() throws Exception {
        when(leadService.getAllLeads(any())).thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/v1/leads"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "lead:read")
    void searchLeads_whenAuthorized_returnsOk() throws Exception {
        when(leadService.searchLeads(eq("test"), any())).thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/v1/leads/search")
                        .param("keyword", "test"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "lead:read")
    void filterByStatus_whenAuthorized_returnsOk() throws Exception {
        when(leadService.filterByStatus(eq(LeadStatus.QUALIFIED), any())).thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/v1/leads/status")
                        .param("status", "QUALIFIED"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "lead:update")
    void updateLead_whenAuthorized_returnsOk() throws Exception {
        UpdateLeadRequest request = new UpdateLeadRequest();
        request.setFirstName("Peter Updated");
        request.setLastName("Parker");
        request.setCompanyName("Daily Bugle");
        request.setEmail("peter@example.com");
        request.setPhoneNumber("9876543210");
        request.setSource(LeadSource.WEBSITE);
        request.setStatus(LeadStatus.CONTACTED);

        when(leadService.updateLead(eq("lead-1"), any())).thenReturn(LeadResponse.builder().id("lead-1").build());

        mockMvc.perform(put("/api/v1/leads/lead-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "lead:convert")
    void convertLeadToClient_whenAuthorized_returnsOk() throws Exception {
        when(leadService.convertLeadToClient("lead-1")).thenReturn(LeadResponse.builder().id("lead-1").build());

        mockMvc.perform(post("/api/v1/leads/lead-1/convert"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "lead:read")
    void convertLeadToClient_whenInsufficientAuthority_returnsForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/leads/lead-1/convert"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "lead:delete")
    void deleteLead_whenAuthorized_returnsNoContent() throws Exception {
        doNothing().when(leadService).deleteLead("lead-1");

        mockMvc.perform(delete("/api/v1/leads/lead-1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = "lead:read")
    void deleteLead_whenInsufficientAuthority_returnsForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/leads/lead-1"))
                .andExpect(status().isForbidden());
    }
}
