package com.viraj.dmabackend.client.service;

import com.viraj.dmabackend.auth.enums.UserStatus;
import com.viraj.dmabackend.client.dto.ClientResponse;
import com.viraj.dmabackend.client.dto.CreateClientRequest;
import com.viraj.dmabackend.client.dto.UpdateClientRequest;
import com.viraj.dmabackend.client.entity.Client;
import com.viraj.dmabackend.client.exception.ClientNotFoundException;
import com.viraj.dmabackend.client.exception.DuplicateClientEmailException;
import com.viraj.dmabackend.client.exception.DuplicateClientGstException;
import com.viraj.dmabackend.client.exception.DuplicateClientPhoneException;
import com.viraj.dmabackend.client.repository.ClientRepository;
import com.viraj.dmabackend.lead.entity.Lead;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientResponse createClient(CreateClientRequest request) {

        validateDuplicateEmail(request.getEmail());

        validateDuplicatePhone(request.getPhoneNumber());

        validateDuplicateGst(request.getGstNumber());

        Client client = buildClient(request);

        Client savedClient = clientRepository.save(client);

        return mapClient(savedClient);
    }

    public Client createClientFromLead(Lead lead) {

        validateDuplicateEmail(lead.getEmail());

        validateDuplicatePhone(lead.getPhoneNumber());

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

    public ClientResponse getClientById(String clientId) {

        Client client = findClientById(clientId);

        return mapClient(client);
    }

    public Page<ClientResponse> getAllClients(Pageable pageable) {

        Page<Client> clients = clientRepository.findByStatusNot(UserStatus.DELETED, pageable);

        return mapToClientResponsePage(clients);
    }

    public Page<ClientResponse> searchClients(String keyword, Pageable pageable) {

        Page<Client> clients = clientRepository.findByCompanyNameContainingIgnoreCaseAndStatusNot(keyword, UserStatus.DELETED, pageable);

        return mapToClientResponsePage(clients);
    }

    public Page<ClientResponse> filterClientsByStatus(UserStatus status, Pageable pageable) {

        Page<Client> clients = clientRepository.findByStatus(status, pageable);

        return mapToClientResponsePage(clients);
    }

    public ClientResponse updateClient(String clientId, UpdateClientRequest request) {

        Client client = findClientById(clientId);

        validateEmailForUpdate(request.getEmail(), client.getId());

        validatePhoneForUpdate(request.getPhoneNumber(), client.getId());

        validateGstForUpdate(request.getGstNumber(), client.getId());

        updateClientFields(client, request);

        Client updatedClient = clientRepository.save(client);

        return mapClient(updatedClient);
    }

    public void deleteClient(String clientId) {

        Client client = findClientById(clientId);

        if (client.getStatus() == UserStatus.DELETED) {
            throw new ClientNotFoundException(clientId);
        }

        client.setStatus(UserStatus.DELETED);

        clientRepository.save(client);
    }


    //Helper Methods
    private Client findClientById(String clientId) {

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException(clientId));

        if (client.getStatus() == UserStatus.DELETED) {
            throw new ClientNotFoundException(clientId);
        }
        return client;
    }

    private void validateDuplicateEmail(String email) {

        if (clientRepository.existsByEmail(email)) {
            throw new DuplicateClientEmailException(email);
        }
    }

    private void validateDuplicatePhone(String phoneNumber) {

        if (clientRepository.existsByPhoneNumber(phoneNumber)) {
            throw new DuplicateClientPhoneException(phoneNumber);
        }
    }

    private void validateDuplicateGst(String gstNumber) {

        if (gstNumber != null
                && !gstNumber.isBlank()
                && clientRepository.existsByGstNumber(gstNumber)) {

            throw new DuplicateClientGstException(gstNumber);
        }
    }

    private void validateEmailForUpdate(String email, String clientId) {

        if (clientRepository.existsByEmailAndIdNot(email, clientId)) {
            throw new DuplicateClientEmailException(email);
        }
    }

    private void validatePhoneForUpdate(String phoneNumber, String clientId) {

        if (clientRepository.existsByPhoneNumberAndIdNot(phoneNumber, clientId)) {
            throw new DuplicateClientPhoneException(phoneNumber);
        }
    }

    private void validateGstForUpdate(String gstNumber, String clientId) {

        if (gstNumber != null
                && !gstNumber.isBlank()
                && clientRepository.existsByGstNumberAndIdNot(gstNumber, clientId)) {
            throw new DuplicateClientGstException(gstNumber);
        }
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

    private ClientResponse mapClient(Client client) {

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
                .build();
    }

    private Page<ClientResponse> mapToClientResponsePage(Page<Client> clients) {

        return clients.map(this::mapClient);
    }
}