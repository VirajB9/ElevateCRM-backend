package com.viraj.dmabackend.client.dto;

import com.viraj.dmabackend.client.enums.ClientStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ClientResponse {

    private String id;

    private String companyName;

    private String contactPerson;

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

    private ClientStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String createdBy;

    private String updatedBy;
}