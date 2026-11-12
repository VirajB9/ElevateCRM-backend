package com.viraj.dmabackend.lead.service.impl;

import com.viraj.dmabackend.client.dto.ClientResponse;
import com.viraj.dmabackend.client.service.ClientService;
import com.viraj.dmabackend.common.util.PaginationUtils;
import com.viraj.dmabackend.exception.BadRequestException;
import com.viraj.dmabackend.lead.dto.CreateLeadRequest;
import com.viraj.dmabackend.lead.dto.LeadResponse;
import com.viraj.dmabackend.lead.dto.UpdateLeadRequest;
import com.viraj.dmabackend.lead.entity.Lead;
import com.viraj.dmabackend.lead.enums.LeadSource;
import com.viraj.dmabackend.lead.enums.LeadStatus;
import com.viraj.dmabackend.lead.event.LeadConvertedEvent;
import com.viraj.dmabackend.lead.exception.LeadNotFoundException;
import com.viraj.dmabackend.lead.mapper.LeadMapper;
import com.viraj.dmabackend.lead.repository.LeadRepository;
import com.viraj.dmabackend.lead.service.LeadService;
import com.viraj.dmabackend.lead.validator.LeadValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeadServiceImpl implements LeadService {

    private final LeadRepository leadRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final LeadMapper leadMapper;
    private final LeadValidator leadValidator;
    private final ClientService clientService;

    @Override
    public LeadResponse createLead(CreateLeadRequest request) {

        leadValidator.validateDuplicateLead(request);
        leadValidator.validateAssignedUser(request.getAssignedUserId());

        Lead lead = buildLead(request);
        Lead savedLead = leadRepository.save(lead);

        return leadMapper.toLeadResponse(savedLead);
    }

    @Transactional
    @Override
    public LeadResponse convertLeadToClient(String leadId) {

        Lead lead = findLeadById(leadId);

        leadValidator.validateLeadConversion(lead);

        com.viraj.dmabackend.common.dto.LeadConversionData data = com.viraj.dmabackend.common.dto.LeadConversionData.builder()
                .leadId(lead.getId())
                .firstName(lead.getFirstName())
                .lastName(lead.getLastName())
                .companyName(lead.getCompanyName())
                .email(lead.getEmail())
                .phoneNumber(lead.getPhoneNumber())
                .website(lead.getWebsite())
                .industry(lead.getIndustry())
                .notes(lead.getNotes())
                .build();

        ClientResponse client = null;
        try {
            client = clientService.createClientFromLead(data);

            lead.setStatus(LeadStatus.CONVERTED);
            lead.setConvertedClientId(client.getId());
            lead.setConvertedAt(LocalDateTime.now(ZoneOffset.UTC));

            Lead savedLead = leadRepository.save(lead);

            com.viraj.dmabackend.common.dto.LeadConversionData eventData = data.toBuilder()
                    .clientId(client.getId())
                    .build();

            eventPublisher.publishEvent(new LeadConvertedEvent(eventData));

            return leadMapper.toLeadResponse(savedLead);
        } catch (Exception ex) {
            if (client != null && client.getId() != null) {
                try {
                    log.warn("Compensating rollback: purging orphaned client [{}] after lead conversion error", client.getId());
                    clientService.purgeOrphanedClient(client.getId());
                } catch (Exception rollbackEx) {
                    log.error("Failed to purge orphaned client [{}]: {}", client.getId(), rollbackEx.getMessage());
                }
            }
            throw ex;
        }
    }

    @Override
    public LeadResponse getLeadById(String leadId) {

        Lead lead = findLeadById(leadId);
        return leadMapper.toLeadResponse(lead);
    }

    @Override
    public Page<LeadResponse> getAllLeads(Pageable pageable) {

        return leadRepository.findByStatusNot(com.viraj.dmabackend.lead.enums.LeadStatus.DELETED, PaginationUtils.clampPageable(pageable))
                .map(leadMapper::toLeadResponse);
    }

    @Override
    public Page<LeadResponse> searchLeads(String keyword, Pageable pageable) {

        return leadRepository.searchLeads(keyword, PaginationUtils.clampPageable(pageable)).map(leadMapper::toLeadResponse);
    }

    @Override
    public Page<LeadResponse> filterByStatus(LeadStatus status, Pageable pageable) {

        if (status == LeadStatus.DELETED) {
            throw new BadRequestException("Querying soft-deleted leads is not permitted");
        }

        return leadRepository.findByStatus(status, PaginationUtils.clampPageable(pageable))
                .map(leadMapper::toLeadResponse);
    }

    @Override
    public Page<LeadResponse> filterBySource(LeadSource source, Pageable pageable) {

        return leadRepository.findBySourceAndStatusNot(source, LeadStatus.DELETED, PaginationUtils.clampPageable(pageable))
                .map(leadMapper::toLeadResponse);
    }

    @Override
    public Page<LeadResponse> filterByAssignedUser(String userId, Pageable pageable) {

        String normalizedUserId = userId != null ? userId.trim() : null;

        leadValidator.validateUserExists(normalizedUserId);
        leadValidator.validateAssignedUserAccess(normalizedUserId);

        return leadRepository.findByAssignedUserIdAndStatusNot(normalizedUserId, LeadStatus.DELETED, PaginationUtils.clampPageable(pageable))
                .map(leadMapper::toLeadResponse);
    }

    @Override
    public LeadResponse updateLead(String leadId, UpdateLeadRequest request) {

        Lead existingLead = findLeadById(leadId);

        leadValidator.validateLeadNotConverted(existingLead);
        leadValidator.validateAssignedUser(request.getAssignedUserId());
        leadValidator.validateDuplicateLeadForUpdate(existingLead, request);
        leadValidator.validateLeadStatus(existingLead.getStatus(), request.getStatus());

        updateLeadFields(existingLead, request);

        Lead updatedLead = leadRepository.save(existingLead);

        return leadMapper.toLeadResponse(updatedLead);
    }

    @Override
    public void deleteLead(String leadId) {

        Lead lead = findLeadById(leadId);
        leadValidator.validateLeadNotConverted(lead);
        lead.setStatus(LeadStatus.DELETED);
        lead.setDeleted(true);
        leadRepository.save(lead);
    }


    // =========================
    // Helper Methods
    // =========================
    private Lead findLeadById(String leadId) {
        String trimmedLeadId = leadId != null ? leadId.trim() : null;
        return leadRepository.findByIdAndStatusNot(trimmedLeadId, LeadStatus.DELETED)
                .orElseThrow(() -> new LeadNotFoundException(leadId));
    }

    private Lead buildLead(CreateLeadRequest request) {

        return Lead.builder()
                .firstName(normalizeTrim(request.getFirstName()))
                .lastName(normalizeTrim(request.getLastName()))
                .companyName(normalizeTrim(request.getCompanyName()))
                .email(normalizeEmail(request.getEmail()))
                .phoneNumber(normalizeTrim(request.getPhoneNumber()))
                .website(normalizeTrim(request.getWebsite()))
                .industry(normalizeTrim(request.getIndustry()))
                .source(request.getSource())
                .assignedUserId(normalizeAssignedUserId(request.getAssignedUserId()))
                .estimatedBudget(normalizeBudget(request.getEstimatedBudget()))
                .requirements(normalizeTrim(request.getRequirements()))
                .notes(normalizeTrim(request.getNotes()))
                .build();
    }

    private void updateLeadFields(Lead lead, UpdateLeadRequest request) {

        lead.setFirstName(normalizeTrim(request.getFirstName()));
        lead.setLastName(normalizeTrim(request.getLastName()));
        lead.setCompanyName(normalizeTrim(request.getCompanyName()));
        lead.setEmail(normalizeEmail(request.getEmail()));
        lead.setPhoneNumber(normalizeTrim(request.getPhoneNumber()));
        lead.setWebsite(normalizeTrim(request.getWebsite()));
        lead.setIndustry(normalizeTrim(request.getIndustry()));
        lead.setSource(request.getSource());
        lead.setStatus(request.getStatus());
        lead.setAssignedUserId(normalizeAssignedUserId(request.getAssignedUserId()));
        lead.setEstimatedBudget(normalizeBudget(request.getEstimatedBudget()));
        lead.setRequirements(normalizeTrim(request.getRequirements()));
        lead.setNotes(normalizeTrim(request.getNotes()));
    }

    private BigDecimal normalizeBudget(BigDecimal budget) {
        return budget != null ? budget.setScale(2, RoundingMode.HALF_UP) : null;
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase();
    }

    private String normalizeTrim(String value) {
        return value != null ? value.trim() : null;
    }

    private String normalizeAssignedUserId(String assignedUserId) {
        if (assignedUserId == null || assignedUserId.isBlank()) {
            return null;
        }
        return assignedUserId.trim();
    }
}
