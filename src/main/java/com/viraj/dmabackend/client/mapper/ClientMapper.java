package com.viraj.dmabackend.client.mapper;

import com.viraj.dmabackend.client.dto.ClientResponse;
import com.viraj.dmabackend.client.entity.Client;
import org.springframework.stereotype.Component;

@Component
public class ClientMapper {

    public ClientResponse toClientResponse(Client client) {
        return ClientResponse.builder()
                .id(client.getId())
                .companyName(client.getCompanyName())
                .contactPerson(client.getContactPerson())
                .email(client.getEmail())
                .phoneNumber(client.getPhoneNumber())
                .website(client.getWebsite())
                .industry(client.getIndustry())
                .gstNumber(client.getGstNumber())
                .address(client.getAddress())
                .city(client.getCity())
                .state(client.getState())
                .country(client.getCountry())
                .postalCode(client.getPostalCode())
                .notes(client.getNotes())
                .status(client.getStatus())
                .createdAt(client.getCreatedAt())
                .updatedAt(client.getUpdatedAt())
                .createdBy(client.getCreatedBy())
                .updatedBy(client.getUpdatedBy())
                .build();
    }
}
