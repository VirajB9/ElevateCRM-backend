package com.viraj.dmabackend.client.dto;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateClientRequest {

    private String companyName;

    private String contactPerson;

    @Email
    private String email;

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
