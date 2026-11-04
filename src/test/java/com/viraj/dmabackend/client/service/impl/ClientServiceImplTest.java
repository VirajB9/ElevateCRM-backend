package com.viraj.dmabackend.client.service.impl;

import com.viraj.dmabackend.client.dto.ClientResponse;
import com.viraj.dmabackend.client.dto.UpdateClientRequest;
import com.viraj.dmabackend.client.entity.Client;
import com.viraj.dmabackend.client.enums.ClientStatus;
import com.viraj.dmabackend.client.mapper.ClientMapper;
import com.viraj.dmabackend.client.repository.ClientRepository;
import com.viraj.dmabackend.client.validator.ClientValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientServiceImplTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientMapper clientMapper;

    @Mock
    private ClientValidator clientValidator;

    @InjectMocks
    private ClientServiceImpl clientService;

    @Test
    void updateClient_UpdatesStatusField() {
        Client client = Client.builder()
                .id("client-1")
                .companyName("Acme")
                .contactPerson("John")
                .email("john@acme.com")
                .phoneNumber("1234567890")
                .status(ClientStatus.ACTIVE)
                .build();

        when(clientRepository.findByIdAndStatusNot("client-1", ClientStatus.ARCHIVED))
                .thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClientResponse expectedResponse = ClientResponse.builder().build();
        when(clientMapper.toClientResponse(any(Client.class))).thenReturn(expectedResponse);

        UpdateClientRequest request = new UpdateClientRequest();
        request.setCompanyName("Acme Corp");
        request.setContactPerson("John Doe");
        request.setEmail("john@acme.com");
        request.setPhoneNumber("1234567890");
        request.setStatus(ClientStatus.INACTIVE);

        clientService.updateClient("client-1", request);

        assertEquals(ClientStatus.INACTIVE, client.getStatus());
        verify(clientValidator).validateClientStatusForUpdate(ClientStatus.INACTIVE);
        verify(clientRepository).save(client);
    }
}
