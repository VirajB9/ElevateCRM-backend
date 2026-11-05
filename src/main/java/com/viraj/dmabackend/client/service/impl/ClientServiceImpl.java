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
import com.viraj.dmabackend.common.dto.LeadConversionData;
import com.viraj.dmabackend.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;
    private final ClientValidator clientValidator;

    @Transactional
    @Override
    public ClientResponse createClient(CreateClientRequest request) {

        clientValidator.validateDuplicateEmail(request.getEmail());
        clientValidator.validateDuplicatePhone(request.getPhoneNumber());
        clientValidator.validateDuplicateGst(request.getGstNumber());

        Client client = buildClient(request);
        Client savedClient = clientRepository.save(client);

        return clientMapper.toClientResponse(savedClient);
    }

    @Transactional
    @Override
    public Client createClientFromLead(LeadConversionData data) {

        clientValidator.validateDuplicateEmail(data.getEmail());
        clientValidator.validateDuplicatePhone(data.getPhoneNumber());

        Client client = Client.builder()
                .companyName(normalizeTrim(data.getCompanyName()))
                .contactPerson(buildContactPerson(data.getFirstName(), data.getLastName()))
                .email(normalizeEmail(data.getEmail()))
                .phoneNumber(normalizeTrim(data.getPhoneNumber()))
                .website(normalizeTrim(data.getWebsite()))
                .industry(normalizeTrim(data.getIndustry()))
                .notes(data.getNotes())
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
        
        Page<Client> clients = clientRepository.searchClients(keyword, pageable);
        return clients.map(clientMapper::toClientResponse);
    }

    @Override
    public Page<ClientResponse> filterClientsByStatus(ClientStatus status, Pageable pageable) {

        if (status == ClientStatus.ARCHIVED) {
            throw new BadRequestException("Querying archived clients is not permitted");
        }

        Page<Client> clients = clientRepository.findByStatus(status, pageable);
        return clients.map(clientMapper::toClientResponse);
    }

    @Transactional
    @Override
    public ClientResponse updateClient(String clientId, UpdateClientRequest request) {

        Client client = findClientById(clientId);

        clientValidator.validateClientStatusForUpdate(request.getStatus());
        clientValidator.validateEmailForUpdate(request.getEmail(), client.getId());
        clientValidator.validatePhoneForUpdate(request.getPhoneNumber(), client.getId());
        clientValidator.validateGstForUpdate(request.getGstNumber(), client.getId());

        updateClientFields(client, request);

        Client updatedClient = clientRepository.save(client);
        return clientMapper.toClientResponse(updatedClient);
    }

    @Transactional
    @Override
    public void deleteClient(String clientId) {

        Client client = findClientById(clientId);

        clientValidator.validateClientDeletable(clientId);

        client.setStatus(ClientStatus.ARCHIVED);
        client.setDeleted(true);

        clientRepository.save(client);
    }


    // =========================
    // Helper Methods
    // =========================
    private Client findClientById(String clientId) {

        return clientRepository.findByIdAndStatusNot(clientId, ClientStatus.ARCHIVED)
                .orElseThrow(() -> new ClientNotFoundException(clientId));
    }

    private Client buildClient(CreateClientRequest request) {

        return Client.builder()
                .companyName(normalizeTrim(request.getCompanyName()))
                .contactPerson(normalizeTrim(request.getContactPerson()))
                .email(normalizeEmail(request.getEmail()))
                .phoneNumber(normalizeTrim(request.getPhoneNumber()))
                .website(normalizeTrim(request.getWebsite()))
                .industry(normalizeTrim(request.getIndustry()))
                .gstNumber(normalizeGstNumber(request.getGstNumber()))
                .address(normalizeTrim(request.getAddress()))
                .city(normalizeTrim(request.getCity()))
                .state(normalizeTrim(request.getState()))
                .country(normalizeTrim(request.getCountry()))
                .postalCode(normalizeTrim(request.getPostalCode()))
                .notes(request.getNotes())
                .build();
    }

    private void updateClientFields(Client client, UpdateClientRequest request) {

        client.setCompanyName(normalizeTrim(request.getCompanyName()));
        client.setContactPerson(normalizeTrim(request.getContactPerson()));
        client.setEmail(normalizeEmail(request.getEmail()));
        client.setPhoneNumber(normalizeTrim(request.getPhoneNumber()));
        client.setWebsite(normalizeTrim(request.getWebsite()));
        client.setIndustry(normalizeTrim(request.getIndustry()));
        client.setGstNumber(normalizeGstNumber(request.getGstNumber()));
        client.setAddress(normalizeTrim(request.getAddress()));
        client.setCity(normalizeTrim(request.getCity()));
        client.setState(normalizeTrim(request.getState()));
        client.setCountry(normalizeTrim(request.getCountry()));
        client.setPostalCode(normalizeTrim(request.getPostalCode()));
        client.setNotes(request.getNotes());
        client.setStatus(request.getStatus());
    }

    private String normalizeGstNumber(String gstNumber) {
        if (gstNumber == null || gstNumber.isBlank()) {
            return null;
        }
        return gstNumber.trim().toUpperCase();
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase();
    }

    private String normalizeTrim(String value) {
        return value != null ? value.trim() : null;
    }

    private String buildContactPerson(String firstName, String lastName) {
        String first = normalizeTrim(firstName);
        String last = normalizeTrim(lastName);

        if (first == null && last == null) {
            return null;
        }
        if (first == null) {
            return last;
        }
        if (last == null) {
            return first;
        }
        return first + " " + last;
    }
}
