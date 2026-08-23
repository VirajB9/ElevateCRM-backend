package com.viraj.dmabackend.lead.service.impl;

import com.viraj.dmabackend.client.entity.Client;
import com.viraj.dmabackend.client.service.ClientService;
import com.viraj.dmabackend.lead.dto.CreateLeadRequest;
import com.viraj.dmabackend.lead.dto.LeadResponse;
import com.viraj.dmabackend.lead.dto.UpdateLeadRequest;
import com.viraj.dmabackend.lead.entity.Lead;
import com.viraj.dmabackend.lead.enums.LeadSource;
import com.viraj.dmabackend.lead.enums.LeadStatus;
import com.viraj.dmabackend.lead.exception.LeadNotFoundException;
import com.viraj.dmabackend.lead.mapper.LeadMapper;
import com.viraj.dmabackend.lead.repository.LeadRepository;
import com.viraj.dmabackend.lead.service.LeadService;
import com.viraj.dmabackend.lead.validator.LeadValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LeadServiceImpl implements LeadService {

    private final LeadRepository leadRepository;
    private final ClientService clientService;
    private final LeadMapper leadMapper;
    private final LeadValidator leadValidator;

    @Override
    public LeadResponse createLead(CreateLeadRequest request) {

        leadValidator.validateDuplicateLead(request);

        Lead lead = buildLead(request);
        Lead savedLead = leadRepository.save(lead);

        return leadMapper.toLeadResponse(savedLead);
    }

    @Override
    public LeadResponse convertLeadToClient(String leadId) {

        Lead lead = findLeadById(leadId);

        leadValidator.validateLeadConversion(lead);

        Client client = clientService.createClientFromLead(lead);

        lead.setConvertedClientId(client.getId());
        lead.setConvertedAt(LocalDateTime.now());

        Lead savedLead = leadRepository.save(lead);

        return leadMapper.toLeadResponse(savedLead);
    }

    @Override
    public LeadResponse getLeadById(String leadId) {

        Lead lead = findLeadById(leadId);
        return leadMapper.toLeadResponse(lead);
    }

    @Override
    public Page<LeadResponse> getAllLeads(Pageable pageable) {

        return leadRepository.findAll(pageable)
                .map(leadMapper::toLeadResponse);
    }

    @Override
    public Page<LeadResponse> searchLeads(String keyword, Pageable pageable) {

        return leadRepository.findByCompanyNameContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                        keyword,
                        keyword,
                        keyword,
                        pageable)
                .map(leadMapper::toLeadResponse);
    }

    @Override
    public Page<LeadResponse> filterByStatus(LeadStatus status, Pageable pageable) {

        return leadRepository.findByStatus(status, pageable)
                .map(leadMapper::toLeadResponse);
    }

    @Override
    public Page<LeadResponse> filterBySource(LeadSource source, Pageable pageable) {

        return leadRepository.findBySource(source, pageable)
                .map(leadMapper::toLeadResponse);
    }

    @Override
    public Page<LeadResponse> filterByAssignedUser(String userId, Pageable pageable) {

        return leadRepository.findByAssignedUserId(userId, pageable)
                .map(leadMapper::toLeadResponse);
    }

    @Override
    public LeadResponse updateLead(String leadId, UpdateLeadRequest request) {

        Lead existingLead = findLeadById(leadId);

        leadValidator.validateDuplicateLeadForUpdate(existingLead, request);
        leadValidator.validateLeadStatus(existingLead.getStatus(), request.getStatus());

        updateLeadFields(existingLead, request);

        Lead updatedLead = leadRepository.save(existingLead);

        return leadMapper.toLeadResponse(updatedLead);
    }

    @Override
    public void deleteLead(String leadId) {

        Lead lead = findLeadById(leadId);
        lead.setStatus(LeadStatus.DELETED);
        leadRepository.save(lead);
    }


    // =========================
    // Helper Methods
    // =========================
    private Lead findLeadById(String leadId) {
        return leadRepository.findById(leadId)
                .orElseThrow(() -> new LeadNotFoundException(leadId));
    }

    private Lead buildLead(CreateLeadRequest request) {

        return Lead.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .companyName(request.getCompanyName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .website(request.getWebsite())
                .industry(request.getIndustry())
                .source(request.getSource())
                .assignedUserId(request.getAssignedUserId())
                .estimatedBudget(request.getEstimatedBudget())
                .requirements(request.getRequirements())
                .notes(request.getNotes())
                .build();
    }

    private void updateLeadFields(Lead lead, UpdateLeadRequest request) {

        lead.setFirstName(request.getFirstName());
        lead.setLastName(request.getLastName());
        lead.setCompanyName(request.getCompanyName());
        lead.setEmail(request.getEmail());
        lead.setPhoneNumber(request.getPhoneNumber());
        lead.setWebsite(request.getWebsite());
        lead.setIndustry(request.getIndustry());
        lead.setSource(request.getSource());
        lead.setStatus(request.getStatus());
        lead.setAssignedUserId(request.getAssignedUserId());
        lead.setEstimatedBudget(request.getEstimatedBudget());
        lead.setRequirements(request.getRequirements());
        lead.setNotes(request.getNotes());
    }
}