package com.viraj.dmabackend.lead.controller;

import com.viraj.dmabackend.lead.dto.CreateLeadRequest;
import com.viraj.dmabackend.lead.dto.LeadResponse;
import com.viraj.dmabackend.lead.dto.UpdateLeadRequest;
import com.viraj.dmabackend.lead.enums.LeadSource;
import com.viraj.dmabackend.lead.service.LeadService;
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

@Tag(name = "5. Lead Management", description = "Endpoints for managing sales leads")
@RestController
@RequestMapping("/api/leads")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class LeadController {

    private final LeadService leadService;

    @PreAuthorize("hasAuthority('lead:create')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create lead", description = "Creates a new lead. Requires 'lead:create' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Lead created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public LeadResponse createLead(
            @Valid @RequestBody CreateLeadRequest request) {
        return leadService.createLead(request);
    }

    @PreAuthorize("hasAuthority('lead:read')")
    @GetMapping("/{leadId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get lead by ID", description = "Retrieves a specific lead by ID. Requires 'lead:read' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lead retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Lead not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public LeadResponse getLeadById(
            @Parameter(description = "Unique ID of the lead", required = true) @PathVariable String leadId) {
        return leadService.getLeadById(leadId);
    }

    @PreAuthorize("hasAuthority('lead:read')")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all leads", description = "Retrieves a paginated list of all leads. Requires 'lead:read' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Leads retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public Page<LeadResponse> getAllLeads(
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        return leadService.getAllLeads(pageable);
    }

    @PreAuthorize("hasAuthority('lead:read')")
    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Search leads", description = "Searches for leads by keyword. Requires 'lead:read' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search results returned successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public Page<LeadResponse> searchLeads(
            @Parameter(description = "Keyword to search", required = true) @RequestParam String keyword,
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        return leadService.searchLeads(keyword, pageable);
    }

    @PreAuthorize("hasAuthority('lead:read')")
    @GetMapping("/source")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Filter leads by source", description = "Retrieves leads filtered by source. Requires 'lead:read' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Filtered leads returned successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public Page<LeadResponse> filterBySource(
            @Parameter(description = "Lead source", required = true) @RequestParam LeadSource source,
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        return leadService.filterBySource(source, pageable);
    }

    @PreAuthorize("hasAuthority('lead:read')")
    @GetMapping("/assigned-user/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Filter leads by assigned user", description = "Retrieves leads assigned to a specific user. Requires 'lead:read' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Filtered leads returned successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public Page<LeadResponse> filterByAssignedUser(
            @Parameter(description = "Unique ID of the assigned user", required = true) @PathVariable String userId,
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        return leadService.filterByAssignedUser(userId, pageable);
    }

    @PreAuthorize("hasAuthority('lead:update')")
    @PutMapping("/{leadId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update lead", description = "Updates a lead's details. Requires 'lead:update' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lead updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Lead not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public LeadResponse updateLead(
            @Parameter(description = "Unique ID of the lead", required = true) @PathVariable String leadId,
            @Valid @RequestBody UpdateLeadRequest request) {
        return leadService.updateLead(leadId, request);
    }

    @PreAuthorize("hasAuthority('lead:convert')")
    @PostMapping("/{leadId}/convert")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Convert lead to client", description = "Converts a lead into a client. Requires 'lead:convert' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lead successfully converted to client"),
            @ApiResponse(responseCode = "400", description = "Lead cannot be converted"),
            @ApiResponse(responseCode = "404", description = "Lead not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public LeadResponse convertLeadToClient(
            @Parameter(description = "Unique ID of the lead", required = true) @PathVariable String leadId) {
        return leadService.convertLeadToClient(leadId);
    }

    @PreAuthorize("hasAuthority('lead:delete')")
    @DeleteMapping("/{leadId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete lead", description = "Deletes a lead. Requires 'lead:delete' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Lead successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Lead not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public void deleteLead(
            @Parameter(description = "Unique ID of the lead", required = true) @PathVariable String leadId) {
        leadService.deleteLead(leadId);
    }
}
