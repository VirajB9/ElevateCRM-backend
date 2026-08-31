package com.viraj.dmabackend.client.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateClientRequest {

    @NotBlank
    private String companyName;

    @NotBlank
    private String contactPerson;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String phoneNumber;

    private String website;

    private String industry;

    private String gstNumber;

    private String address;

    private String city;

    private String state;

    private String country;

    private String postalCode;

    private String notes;
}
