package com.viraj.dmabackend.client.controller;

import com.viraj.dmabackend.auth.enums.UserStatus;
import com.viraj.dmabackend.client.dto.ClientResponse;
import com.viraj.dmabackend.client.dto.CreateClientRequest;
import com.viraj.dmabackend.client.dto.UpdateClientRequest;
import com.viraj.dmabackend.client.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "5. Client Management")
@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class ClientController {

    private final ClientService clientService;

    @PreAuthorize("hasAuthority('client:create')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create Client")
    public ClientResponse createClient(
            @Valid @RequestBody CreateClientRequest request) {

        return clientService.createClient(request);
    }

    @PreAuthorize("hasAuthority('client:read')")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get All Clients")
    public Page<ClientResponse> getAllClients(Pageable pageable) {

        return clientService.getAllClients(pageable);
    }

    @PreAuthorize("hasAuthority('client:read')")
    @GetMapping("/{clientId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get Client By Id")
    public ClientResponse getClientById(
            @PathVariable String clientId) {

        return clientService.getClientById(clientId);
    }

    @PreAuthorize("hasAuthority('client:read')")
    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Search Clients")
    public Page<ClientResponse> searchClients(
            @RequestParam String keyword,
            Pageable pageable) {

        return clientService.searchClients(keyword, pageable);
    }

    @PreAuthorize("hasAuthority('client:read')")
    @GetMapping("/status")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Filter Clients By Status")
    public Page<ClientResponse> filterClientsByStatus(
            @RequestParam UserStatus status,
            Pageable pageable) {

        return clientService.filterClientsByStatus(status, pageable);
    }

    @PreAuthorize("hasAuthority('client:update')")
    @PutMapping("/{clientId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update Client")
    public ClientResponse updateClient(
            @PathVariable String clientId,
            @Valid @RequestBody UpdateClientRequest request) {

        return clientService.updateClient(clientId, request);
    }

    @PreAuthorize("hasAuthority('client:delete')")
    @DeleteMapping("/{clientId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete Client")
    public void deleteClient(
            @PathVariable String clientId) {

        clientService.deleteClient(clientId);
    }
}