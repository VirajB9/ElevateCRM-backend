package com.viraj.dmabackend.client.service.impl;

import com.viraj.dmabackend.client.dto.CreateClientRequest;
import com.viraj.dmabackend.client.dto.ClientResponse;
import com.viraj.dmabackend.client.dto.UpdateClientRequest;
import com.viraj.dmabackend.client.entity.Client;
import com.viraj.dmabackend.client.enums.ClientStatus;
import com.viraj.dmabackend.client.mapper.ClientMapper;
import com.viraj.dmabackend.client.repository.ClientRepository;
import com.viraj.dmabackend.client.validator.ClientValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
    void createClient_NormalizesBlankGstNumberToNull() {
        CreateClientRequest request = new CreateClientRequest();
        request.setCompanyName(" Acme Corp ");
        request.setContactPerson(" John Doe ");
        request.setEmail("john@acme.com");
        request.setPhoneNumber("1234567890");
        request.setGstNumber("   ");

        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(clientMapper.toClientResponse(any(Client.class))).thenReturn(ClientResponse.builder().build());

        clientService.createClient(request);

        ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
        verify(clientRepository).save(captor.capture());
        Client saved = captor.getValue();
        assertEquals("Acme Corp", saved.getCompanyName());
        assertEquals("John Doe", saved.getContactPerson());
        assertNull(saved.getGstNumber());
    }

    @Test
    void updateClient_NormalizesBlankGstNumberToNull() {
        Client client = Client.builder()
                .id("client-1")
                .companyName("Acme")
                .contactPerson("John")
                .email("john@acme.com")
                .phoneNumber("1234567890")
                .gstNumber("27ABCDE1234F1Z5")
                .status(ClientStatus.ACTIVE)
                .build();

        when(clientRepository.findByIdAndStatusNot("client-1", ClientStatus.ARCHIVED))
                .thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(clientMapper.toClientResponse(any(Client.class))).thenReturn(ClientResponse.builder().build());

        UpdateClientRequest request = new UpdateClientRequest();
        request.setCompanyName("Acme Corp");
        request.setContactPerson("John Doe");
        request.setEmail("john@acme.com");
        request.setPhoneNumber("1234567890");
        request.setGstNumber("");
        request.setStatus(ClientStatus.ACTIVE);

        clientService.updateClient("client-1", request);

        ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
        verify(clientRepository).save(captor.capture());
        Client saved = captor.getValue();
        assertNull(saved.getGstNumber());
    }

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
        verify(clientValidator).validateClientStatusForUpdate(ClientStatus.INACTIVE, "client-1");
        verify(clientRepository).save(client);
    }

    @Test
    void createClientFromLead_BuildsContactPersonWithoutNullConcatenation() {
        com.viraj.dmabackend.common.dto.LeadConversionData data = com.viraj.dmabackend.common.dto.LeadConversionData.builder()
                .leadId("lead-1")
                .firstName("  Jane  ")
                .lastName(null)
                .companyName("  Acme Inc  ")
                .email("  jane@acme.com  ")
                .phoneNumber("  9876543210  ")
                .website("  https://acme.com  ")
                .industry("  Tech  ")
                .notes("Some note")
                .build();

        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));

        clientService.createClientFromLead(data);

        ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
        verify(clientRepository).save(captor.capture());
        Client saved = captor.getValue();
        assertEquals("Jane", saved.getContactPerson());
        assertEquals("Acme Inc", saved.getCompanyName());
        assertEquals("jane@acme.com", saved.getEmail());
        assertEquals("9876543210", saved.getPhoneNumber());
        assertEquals("https://acme.com", saved.getWebsite());
        assertEquals("Tech", saved.getIndustry());
    }

    @Test
    void createClient_NormalizesEmailToLowercase() {
        CreateClientRequest request = new CreateClientRequest();
        request.setCompanyName("Acme");
        request.setContactPerson("John");
        request.setEmail("  John.Doe@ACME.COM  ");
        request.setPhoneNumber("1234567890");

        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(clientMapper.toClientResponse(any(Client.class))).thenReturn(ClientResponse.builder().build());

        clientService.createClient(request);

        ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
        verify(clientRepository).save(captor.capture());
        assertEquals("john.doe@acme.com", captor.getValue().getEmail());
    }

    @Test
    void updateClient_NormalizesEmailToLowercase() {
        Client client = Client.builder()
                .id("client-1")
                .companyName("Acme")
                .contactPerson("John")
                .email("old@acme.com")
                .phoneNumber("1234567890")
                .status(ClientStatus.ACTIVE)
                .build();

        UpdateClientRequest request = new UpdateClientRequest();
        request.setCompanyName("Acme");
        request.setContactPerson("John");
        request.setEmail("  Updated.John@ACME.COM  ");
        request.setPhoneNumber("1234567890");
        request.setStatus(ClientStatus.ACTIVE);

        when(clientRepository.findByIdAndStatusNot("client-1", ClientStatus.ARCHIVED))
                .thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(clientMapper.toClientResponse(any(Client.class))).thenReturn(ClientResponse.builder().build());

        clientService.updateClient("client-1", request);

        assertEquals("updated.john@acme.com", client.getEmail());
    }
}
