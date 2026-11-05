package com.viraj.dmabackend.lead.mapper;

import com.viraj.dmabackend.lead.dto.LeadResponse;
import com.viraj.dmabackend.lead.entity.Lead;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LeadMapperTest {

    private final LeadMapper leadMapper = new LeadMapper();

    @Test
    void toLeadResponse_whenLeadIsNull_returnsNullWithoutNPE() {
        LeadResponse response = leadMapper.toLeadResponse(null);
        assertThat(response).isNull();
    }

    @Test
    void toLeadResponse_whenLeadIsNotNull_mapsAllFields() {
        Lead lead = Lead.builder()
                .id("lead-1")
                .firstName("John")
                .lastName("Doe")
                .companyName("Acme")
                .build();

        LeadResponse response = leadMapper.toLeadResponse(lead);
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("lead-1");
        assertThat(response.getFirstName()).isEqualTo("John");
        assertThat(response.getLastName()).isEqualTo("Doe");
    }
}
