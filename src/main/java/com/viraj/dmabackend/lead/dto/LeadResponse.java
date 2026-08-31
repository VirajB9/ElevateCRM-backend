package com.viraj.dmabackend.lead.dto;

import com.viraj.dmabackend.lead.enums.LeadSource;
import com.viraj.dmabackend.lead.enums.LeadStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class LeadResponse {

    private String id;

    private String firstName;

    private String lastName;

    private String companyName;

    private String email;

    private String phoneNumber;

    private String website;

    private String industry;

    private LeadSource source;

    private LeadStatus status;

    private String assignedUserId;

    private BigDecimal estimatedBudget;

    private String requirements;

    private String notes;

    private String convertedClientId;

    private LocalDateTime convertedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String createdBy;

    private String updatedBy;
}
