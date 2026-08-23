package com.viraj.dmabackend.lead.mapper;

import com.viraj.dmabackend.lead.dto.LeadResponse;
import com.viraj.dmabackend.lead.entity.Lead;
import org.springframework.stereotype.Component;

@Component
public class LeadMapper {

    public LeadResponse toLeadResponse(Lead lead) {
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
