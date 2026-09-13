package com.viraj.dmabackend.lead.dto;

import com.viraj.dmabackend.lead.enums.LeadSource;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateLeadRequest {

    @NotBlank(message = "First name is required.")
    
    @jakarta.validation.constraints.Size(max = 50)
    private String firstName;

    @NotBlank(message = "Last name is required.")
    
    @jakarta.validation.constraints.Size(max = 50)
    private String lastName;

    @NotBlank(message = "Company name is required.")
    
    @jakarta.validation.constraints.Size(max = 150)
    private String companyName;

    @NotBlank(message = "Email is required.")
    @Email(message = "Invalid email format.")
    
    @jakarta.validation.constraints.Size(max = 150)
    private String email;

    @NotBlank(message = "Phone number is required.")
    
    @jakarta.validation.constraints.Size(max = 20)
    private String phoneNumber;

    
    @jakarta.validation.constraints.Size(max = 255)
    private String website;

    
    @jakarta.validation.constraints.Size(max = 100)
    private String industry;

    @NotNull(message = "Lead source is required.")
    private LeadSource source;

    
    @jakarta.validation.constraints.Size(max = 50)
    private String assignedUserId;

    @DecimalMin(value = "0.0", inclusive = true, message = "Estimated budget cannot be negative.")
    private BigDecimal estimatedBudget;

    
    @jakarta.validation.constraints.Size(max = 2000)
    private String requirements;

    
    @jakarta.validation.constraints.Size(max = 2000)
    private String notes;
}
