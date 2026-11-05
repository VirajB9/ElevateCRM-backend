package com.viraj.dmabackend.client.mapper;

import com.viraj.dmabackend.client.dto.ClientResponse;
import com.viraj.dmabackend.client.entity.Client;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClientMapperTest {

    private final ClientMapper clientMapper = new ClientMapper();

    @Test
    void toClientResponse_whenClientIsNull_returnsNullWithoutNPE() {
        ClientResponse response = clientMapper.toClientResponse(null);
        assertThat(response).isNull();
    }

    @Test
    void toClientResponse_whenClientIsNotNull_mapsAllFields() {
        Client client = Client.builder()
                .id("client-1")
                .companyName("Acme Corp")
                .contactPerson("Jane Doe")
                .build();

        ClientResponse response = clientMapper.toClientResponse(client);
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("client-1");
        assertThat(response.getCompanyName()).isEqualTo("Acme Corp");
        assertThat(response.getContactPerson()).isEqualTo("Jane Doe");
    }
}
