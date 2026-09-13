package com.viraj.dmabackend.client.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateClientRequest {

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
    @Size(max = 20)
    private String phoneNumber;

    @Size(max = 255)
    private String website;

    @Size(max = 100)
    private String industry;

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
}
