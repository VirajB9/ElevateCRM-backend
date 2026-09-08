package com.viraj.dmabackend.common.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LeadConversionData {
    private final String leadId;
    private final String firstName;
    private final String lastName;
    private final String companyName;
    private final String email;
    private final String phoneNumber;
    private final String website;
    private final String industry;
    private final String notes;
}
