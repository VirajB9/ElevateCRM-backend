package com.viraj.dmabackend.lead.service;

import com.viraj.dmabackend.client.entity.Client;
import com.viraj.dmabackend.client.service.ClientService;
import com.viraj.dmabackend.lead.dto.CreateLeadRequest;
import com.viraj.dmabackend.lead.dto.LeadResponse;
import com.viraj.dmabackend.lead.dto.UpdateLeadRequest;
import com.viraj.dmabackend.lead.entity.Lead;
import com.viraj.dmabackend.lead.enums.LeadSource;
import com.viraj.dmabackend.lead.enums.LeadStatus;
import com.viraj.dmabackend.lead.exception.DuplicateLeadException;
import com.viraj.dmabackend.lead.exception.InvalidLeadStatusException;
import com.viraj.dmabackend.lead.exception.LeadAlreadyConvertedException;
import com.viraj.dmabackend.lead.exception.LeadNotFoundException;
import com.viraj.dmabackend.lead.repository.LeadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LeadService {

    private final LeadRepository leadRepository;
    private final ClientService clientService;

    public LeadResponse createLead(CreateLeadRequest request) {

        validateDuplicateLead(request);

        Lead lead = buildLead(request);

        Lead savedLead = leadRepository.save(lead);

        return mapToLeadResponse(savedLead);
    }

    public LeadResponse convertLeadToClient(String leadId) {

        Lead lead = findLeadById(leadId);

        validateLeadConversion(lead);

        Client client = clientService.createClientFromLead(lead);

        lead.setConvertedClientId(client.getId());

        lead.setConvertedAt(LocalDateTime.now());

        Lead savedLead = leadRepository.save(lead);

        return mapToLeadResponse(savedLead);
    }

    public LeadResponse getLeadById(String leadId) {

        Lead lead = findLeadById(leadId);

        return mapToLeadResponse(lead);
    }

    public Page<LeadResponse> getAllLeads(Pageable pageable) {

        return leadRepository.findAll(pageable)
                .map(this::mapToLeadResponse);
    }

    public Page<LeadResponse> searchLeads(String keyword, Pageable pageable) {

        return leadRepository.findByCompanyNameContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                        keyword,
                        keyword,
                        keyword,
                        pageable)
                .map(this::mapToLeadResponse);
    }

    public Page<LeadResponse> filterByStatus(LeadStatus status, Pageable pageable) {

        return leadRepository.findByStatus(status, pageable)
                .map(this::mapToLeadResponse);
    }

    public Page<LeadResponse> filterBySource(LeadSource source, Pageable pageable) {

        return leadRepository.findBySource(source, pageable)
                .map(this::mapToLeadResponse);
    }

    public Page<LeadResponse> filterByAssignedUser(String userId, Pageable pageable) {

        return leadRepository.findByAssignedUserId(userId, pageable)
                .map(this::mapToLeadResponse);
    }

    public LeadResponse updateLead(String leadId, UpdateLeadRequest request) {

        Lead existingLead = findLeadById(leadId);

        validateDuplicateLeadForUpdate(existingLead, request);

        validateLeadStatus(existingLead.getStatus(), request.getStatus());

        updateLeadFields(existingLead, request);

        Lead updatedLead = leadRepository.save(existingLead);

        return mapToLeadResponse(updatedLead);
    }

    public void deleteLead(String leadId) {

        Lead lead = findLeadById(leadId);

        leadRepository.delete(lead);
    }


    // Helper methods
    private Lead findLeadById(String leadId) {
        return leadRepository.findById(leadId)
                .orElseThrow(() -> new LeadNotFoundException(leadId));
    }

    private void validateDuplicateLead(CreateLeadRequest request) {

        if (leadRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateLeadException("email", request.getEmail());
        }

        if (leadRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new DuplicateLeadException("phone number", request.getPhoneNumber());
        }
    }

    private void validateDuplicateLeadForUpdate(Lead existingLead, UpdateLeadRequest request) {

        leadRepository.findByEmail(request.getEmail())
                .ifPresent(lead -> {
                    if (!lead.getId().equals(existingLead.getId())) {
                        throw new DuplicateLeadException("email", request.getEmail());
                    }
                });

        leadRepository.findByPhoneNumber(request.getPhoneNumber())
                .ifPresent(lead -> {
                    if (!lead.getId().equals(existingLead.getId())) {
                        throw new DuplicateLeadException("phone number", request.getPhoneNumber());
                    }
                });
    }

    private void validateLeadStatus(LeadStatus currentStatus, LeadStatus newStatus) {

        if (currentStatus == newStatus) {
            return;
        }

        switch (currentStatus) {
            case NEW -> {
                if (newStatus != LeadStatus.CONTACTED && newStatus != LeadStatus.LOST) {
                    throw new InvalidLeadStatusException(currentStatus, newStatus);
                }
            }

            case CONTACTED -> {
                if (newStatus != LeadStatus.QUALIFIED && newStatus != LeadStatus.LOST) {
                    throw new InvalidLeadStatusException(currentStatus, newStatus);
                }
            }

            case QUALIFIED -> {
                if (newStatus != LeadStatus.PROPOSAL_SENT && newStatus != LeadStatus.LOST) {
                    throw new InvalidLeadStatusException(currentStatus, newStatus);
                }
            }

            case PROPOSAL_SENT -> {
                if (newStatus != LeadStatus.NEGOTIATION && newStatus != LeadStatus.LOST) {
                    throw new InvalidLeadStatusException(currentStatus, newStatus);
                }
            }

            case NEGOTIATION -> {
                if (newStatus != LeadStatus.WON && newStatus != LeadStatus.LOST) {
                    throw new InvalidLeadStatusException(currentStatus, newStatus);
                }
            }

            case WON, LOST -> throw new InvalidLeadStatusException(currentStatus, newStatus);
        }
    }

    private void validateLeadConversion(Lead lead) {

        if (lead.getConvertedClientId() != null) {
            throw new LeadAlreadyConvertedException(lead.getId());
        }

        if (lead.getStatus() != LeadStatus.WON) {
            throw new InvalidLeadStatusException(lead.getStatus(), LeadStatus.WON);
        }
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

    private LeadResponse mapToLeadResponse(Lead lead) {

        return LeadResponse.builder()
                .id(lead.getId())
                .firstName(lead.getFirstName())
                .lastName(lead.getLastName())
                .companyName(lead.getCompanyName())
                .email(lead.getEmail())
                .phoneNumber(lead.getPhoneNumber())
                .website(lead.getWebsite())
                .industry(lead.getIndustry())
                .source(lead.getSource())
                .status(lead.getStatus())
                .assignedUserId(lead.getAssignedUserId())
                .estimatedBudget(lead.getEstimatedBudget())
                .requirements(lead.getRequirements())
                .notes(lead.getNotes())
                .convertedClientId(lead.getConvertedClientId())
                .convertedAt(lead.getConvertedAt())
                .createdAt(lead.getCreatedAt())
                .updatedAt(lead.getUpdatedAt())
                .createdBy(lead.getCreatedBy())
                .updatedBy(lead.getUpdatedBy())
                .build();
    }
}