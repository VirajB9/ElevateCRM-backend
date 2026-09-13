package com.viraj.dmabackend.auth.dto;

import jakarta.validation.constraints.Size;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequest {

    @NotBlank(message = "Current password is required")
    
    @jakarta.validation.constraints.Size(max = 100)
    private String currentPassword;

    @NotBlank(message = "New password is required")
    
    @jakarta.validation.constraints.Size(max = 100)
    private String newPassword;
}
