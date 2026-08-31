package com.viraj.dmabackend.lead.dto;

import com.viraj.dmabackend.lead.enums.LeadSource;
import com.viraj.dmabackend.lead.enums.LeadStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateLeadRequest {

    @NotBlank(message = "First name is required.")
    private String firstName;

    @NotBlank(message = "Last name is required.")
    private String lastName;

    @NotBlank(message = "Company name is required.")
    private String companyName;

    @NotBlank(message = "Email is required.")
    @Email(message = "Invalid email format.")
    private String email;

    @NotBlank(message = "Phone number is required.")
    private String phoneNumber;

    private String website;

    private String industry;

    @NotNull(message = "Lead source is required.")
    private LeadSource source;

    @NotNull(message = "Lead status is required.")
    private LeadStatus status;

    private String assignedUserId;

    @DecimalMin(value = "0.0", inclusive = true, message = "Estimated budget cannot be negative.")
    private BigDecimal estimatedBudget;

    private String requirements;

    private String notes;
}
