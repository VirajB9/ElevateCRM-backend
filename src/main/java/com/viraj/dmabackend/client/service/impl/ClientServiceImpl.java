package com.viraj.dmabackend.client.service.impl;

import com.viraj.dmabackend.client.enums.ClientStatus;
import com.viraj.dmabackend.client.dto.ClientResponse;
import com.viraj.dmabackend.client.dto.CreateClientRequest;
import com.viraj.dmabackend.client.dto.UpdateClientRequest;
import com.viraj.dmabackend.client.entity.Client;
import com.viraj.dmabackend.client.exception.ClientNotFoundException;
import com.viraj.dmabackend.client.mapper.ClientMapper;
import com.viraj.dmabackend.client.repository.ClientRepository;
import com.viraj.dmabackend.client.service.ClientService;
import com.viraj.dmabackend.client.validator.ClientValidator;
import com.viraj.dmabackend.lead.entity.Lead;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;
    private final ClientValidator clientValidator;

    @Override
    public ClientResponse createClient(CreateClientRequest request) {

        clientValidator.validateDuplicateEmail(request.getEmail());
        clientValidator.validateDuplicatePhone(request.getPhoneNumber());
        clientValidator.validateDuplicateGst(request.getGstNumber());

        Client client = buildClient(request);
        Client savedClient = clientRepository.save(client);

        return clientMapper.toClientResponse(savedClient);
    }

    @Override
    public Client createClientFromLead(Lead lead) {

        clientValidator.validateDuplicateEmail(lead.getEmail());
        clientValidator.validateDuplicatePhone(lead.getPhoneNumber());

        Client client = Client.builder()
                .companyName(lead.getCompanyName())
                .contactPerson(lead.getFirstName() + " " + lead.getLastName())
                .email(lead.getEmail())
                .phoneNumber(lead.getPhoneNumber())
                .website(lead.getWebsite())
                .industry(lead.getIndustry())
                .notes(lead.getNotes())
                .build();

        return clientRepository.save(client);
    }

    @Override
    public ClientResponse getClientById(String clientId) {

        Client client = findClientById(clientId);
        return clientMapper.toClientResponse(client);
    }

    @Override
    public Page<ClientResponse> getAllClients(Pageable pageable) {

        Page<Client> clients = clientRepository.findByStatusNot(ClientStatus.ARCHIVED, pageable);
        return clients.map(clientMapper::toClientResponse);
    }

    @Override
    public Page<ClientResponse> searchClients(String keyword, Pageable pageable) {

        Page<Client> clients = clientRepository.findByCompanyNameContainingIgnoreCaseAndStatusNot(keyword, ClientStatus.ARCHIVED, pageable);
        return clients.map(clientMapper::toClientResponse);
    }

    @Override
    public Page<ClientResponse> filterClientsByStatus(ClientStatus status, Pageable pageable) {

        Page<Client> clients = clientRepository.findByStatus(status, pageable);
        return clients.map(clientMapper::toClientResponse);
    }

    @Override
    public ClientResponse updateClient(String clientId, UpdateClientRequest request) {

        Client client = findClientById(clientId);

        clientValidator.validateEmailForUpdate(request.getEmail(), client.getId());
        clientValidator.validatePhoneForUpdate(request.getPhoneNumber(), client.getId());
        clientValidator.validateGstForUpdate(request.getGstNumber(), client.getId());

        updateClientFields(client, request);

        Client updatedClient = clientRepository.save(client);
        return clientMapper.toClientResponse(updatedClient);
    }

    @Override
    public void deleteClient(String clientId) {

        Client client = findClientById(clientId);

        if (client.getStatus() == ClientStatus.ARCHIVED) {
            throw new ClientNotFoundException(clientId);
        }

        client.setStatus(ClientStatus.ARCHIVED);
        clientRepository.save(client);
    }


    // =========================
    // Helper Methods
    // =========================
    private Client findClientById(String clientId) {

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException(clientId));

        if (client.getStatus() == ClientStatus.ARCHIVED) {
            throw new ClientNotFoundException(clientId);
        }
        return client;
    }

    private Client buildClient(CreateClientRequest request) {

        return Client.builder()
                .companyName(request.getCompanyName())
                .contactPerson(request.getContactPerson())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .website(request.getWebsite())
                .industry(request.getIndustry())
                .gstNumber(request.getGstNumber())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .postalCode(request.getPostalCode())
                .notes(request.getNotes())
                .build();
    }

    private void updateClientFields(Client client, UpdateClientRequest request) {

        client.setCompanyName(request.getCompanyName());
        client.setContactPerson(request.getContactPerson());
        client.setEmail(request.getEmail());
        client.setPhoneNumber(request.getPhoneNumber());
        client.setWebsite(request.getWebsite());
        client.setIndustry(request.getIndustry());
        client.setGstNumber(request.getGstNumber());
        client.setAddress(request.getAddress());
        client.setCity(request.getCity());
        client.setState(request.getState());
        client.setCountry(request.getCountry());
        client.setPostalCode(request.getPostalCode());
        client.setNotes(request.getNotes());
    }
}