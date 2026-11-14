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
    @Size(min = 8, max = 100, message = "New password must be between 8 and 100 characters")
    @jakarta.validation.constraints.Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?])\\S+$",
            message = "New password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character, and cannot contain whitespace")
    private String newPassword;
}
