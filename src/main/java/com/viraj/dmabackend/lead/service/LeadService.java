package com.viraj.dmabackend.lead.service;

import com.viraj.dmabackend.lead.dto.CreateLeadRequest;
import com.viraj.dmabackend.lead.dto.LeadResponse;
import com.viraj.dmabackend.lead.dto.UpdateLeadRequest;
import com.viraj.dmabackend.lead.enums.LeadSource;
import com.viraj.dmabackend.lead.enums.LeadStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LeadService {
    LeadResponse createLead(CreateLeadRequest request);
    LeadResponse convertLeadToClient(String leadId);
    LeadResponse getLeadById(String leadId);
    Page<LeadResponse> getAllLeads(Pageable pageable);
    Page<LeadResponse> searchLeads(String keyword, Pageable pageable);
    Page<LeadResponse> filterByStatus(LeadStatus status, Pageable pageable);
    Page<LeadResponse> filterBySource(LeadSource source, Pageable pageable);
    Page<LeadResponse> filterByAssignedUser(String userId, Pageable pageable);
    LeadResponse updateLead(String leadId, UpdateLeadRequest request);
    void deleteLead(String leadId);
}
