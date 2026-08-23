package com.viraj.dmabackend.client.service;

import com.viraj.dmabackend.client.dto.ClientResponse;
import com.viraj.dmabackend.client.dto.CreateClientRequest;
import com.viraj.dmabackend.client.dto.UpdateClientRequest;
import com.viraj.dmabackend.client.entity.Client;
import com.viraj.dmabackend.client.enums.ClientStatus;
import com.viraj.dmabackend.lead.entity.Lead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClientService {
    ClientResponse createClient(CreateClientRequest request);
    Client createClientFromLead(Lead lead);
    ClientResponse getClientById(String clientId);
    Page<ClientResponse> getAllClients(Pageable pageable);
    Page<ClientResponse> searchClients(String keyword, Pageable pageable);
    Page<ClientResponse> filterClientsByStatus(ClientStatus status, Pageable pageable);
    ClientResponse updateClient(String clientId, UpdateClientRequest request);
    void deleteClient(String clientId);
}
