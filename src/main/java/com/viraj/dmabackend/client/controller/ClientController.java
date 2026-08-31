package com.viraj.dmabackend.client.controller;

import com.viraj.dmabackend.client.enums.ClientStatus;
import com.viraj.dmabackend.client.dto.ClientResponse;
import com.viraj.dmabackend.client.dto.CreateClientRequest;
import com.viraj.dmabackend.client.dto.UpdateClientRequest;
import com.viraj.dmabackend.client.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "6. Client Management", description = "Endpoints for managing agency clients")
@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class ClientController {

    private final ClientService clientService;

    @PreAuthorize("hasAuthority('client:create')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create Client", description = "Registers a new client. Requires 'client:create' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Client created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public ClientResponse createClient(
            @Valid @RequestBody CreateClientRequest request) {
        return clientService.createClient(request);
    }

    @PreAuthorize("hasAuthority('client:read')")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get All Clients", description = "Retrieves a paginated list of all clients. Requires 'client:read' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Clients retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public Page<ClientResponse> getAllClients(
            @Parameter(description = "Pagination and sorting parameters") Pageable pageable) {
        return clientService.getAllClients(pageable);
    }

    @PreAuthorize("hasAuthority('client:read')")
    @GetMapping("/{clientId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get Client By Id", description = "Retrieves a specific client by ID. Requires 'client:read' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Client not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public ClientResponse getClientById(
            @Parameter(description = "Unique ID of the client", required = true)
            @PathVariable String clientId) {
        return clientService.getClientById(clientId);
    }

    @PreAuthorize("hasAuthority('client:read')")
    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Search Clients", description = "Searches for clients by keyword. Requires 'client:read' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search results returned successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public Page<ClientResponse> searchClients(
            @Parameter(description = "Keyword to search for in client details", required = true) @RequestParam String keyword,
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        return clientService.searchClients(keyword, pageable);
    }

    @PreAuthorize("hasAuthority('client:read')")
    @GetMapping("/status")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Filter Clients By Status", description = "Retrieves a paginated list of clients filtered by status. Requires 'client:read' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Filtered clients returned successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public Page<ClientResponse> filterClientsByStatus(
            @Parameter(description = "Exact client status to filter by", required = true) @RequestParam ClientStatus status,
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        return clientService.filterClientsByStatus(status, pageable);
    }

    @PreAuthorize("hasAuthority('client:update')")
    @PutMapping("/{clientId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update Client", description = "Updates a client's details. Requires 'client:update' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Client not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public ClientResponse updateClient(
            @Parameter(description = "Unique ID of the client", required = true) @PathVariable String clientId,
            @Valid @RequestBody UpdateClientRequest request) {
        return clientService.updateClient(clientId, request);
    }

    @PreAuthorize("hasAuthority('client:delete')")
    @DeleteMapping("/{clientId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete Client", description = "Deletes or archives a client. Requires 'client:delete' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Client successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Client not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public void deleteClient(
            @Parameter(description = "Unique ID of the client", required = true) @PathVariable String clientId) {
        clientService.deleteClient(clientId);
    }
}
