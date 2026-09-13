package com.viraj.dmabackend.auth.dto;

import jakarta.validation.constraints.Size;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email")
    
    @jakarta.validation.constraints.Size(max = 150)
    private String email;

    @NotBlank(message = "Password is required")
    
    @jakarta.validation.constraints.Size(max = 100)
    private String password;
}
