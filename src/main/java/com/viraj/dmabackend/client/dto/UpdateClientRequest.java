package com.viraj.dmabackend.client.dto;

import com.viraj.dmabackend.client.enums.ClientStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateClientRequest {

    @NotBlank
    @Size(max = 150)
    private String companyName;

    @NotBlank
    @Size(max = 100)
    private String contactPerson;

    @NotBlank
    @Email
    @Size(max = 150)
    private String email;

    @NotBlank
    @jakarta.validation.constraints.Pattern(regexp = "^\\s*\\+?[0-9]{10,15}\\s*$", message = "Phone number must be a valid phone number with 10 to 15 digits")
    @Size(max = 20)
    private String phoneNumber;

    @Size(max = 255)
    private String website;

    @Size(max = 100)
    private String industry;

    @jakarta.validation.constraints.Pattern(
            regexp = "^$|^\\s*$|(?i)^\\s*[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}\\s*$",
            message = "GST number must be a valid 15-character GSTIN format")
    @Size(max = 50)
    private String gstNumber;

    @Size(max = 500)
    private String address;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String state;

    @Size(max = 100)
    private String country;

    @Size(max = 20)
    private String postalCode;

    @Size(max = 2000)
    private String notes;

    @NotNull
    private ClientStatus status;
}
