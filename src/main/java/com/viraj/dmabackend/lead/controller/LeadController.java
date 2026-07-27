package com.viraj.dmabackend.lead.controller;

import com.viraj.dmabackend.lead.dto.CreateLeadRequest;
import com.viraj.dmabackend.lead.dto.LeadResponse;
import com.viraj.dmabackend.lead.dto.UpdateLeadRequest;
import com.viraj.dmabackend.lead.enums.LeadSource;
import com.viraj.dmabackend.lead.service.LeadService;
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

@Tag(name = "7. Lead Management")
@RestController
@RequestMapping("/api/leads")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class LeadController {

    private final LeadService leadService;

    @PreAuthorize("hasAuthority('lead:create')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create lead")
    public LeadResponse createLead(
            @Valid @RequestBody CreateLeadRequest request) {

        return leadService.createLead(request);
    }

    @PreAuthorize("hasAuthority('lead:read')")
    @GetMapping("/{leadId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get lead by ID")
    public LeadResponse getLeadById(
            @PathVariable String leadId) {

        return leadService.getLeadById(leadId);
    }

    @PreAuthorize("hasAuthority('lead:read')")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all leads")
    public Page<LeadResponse> getAllLeads(
            Pageable pageable) {

        return leadService.getAllLeads(pageable);
    }

    @PreAuthorize("hasAuthority('lead:read')")
    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Search leads")
    public Page<LeadResponse> searchLeads(
            @RequestParam String keyword, Pageable pageable) {

        return leadService.searchLeads(keyword, pageable);
    }

    @PreAuthorize("hasAuthority('lead:read')")
    @GetMapping("/source")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Filter leads by source")
    public Page<LeadResponse> filterBySource(
            @RequestParam LeadSource source, Pageable pageable) {

        return leadService.filterBySource(source, pageable);
    }

    @PreAuthorize("hasAuthority('lead:read')")
    @GetMapping("/assigned-user/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Filter leads by assigned user")
    public Page<LeadResponse> filterByAssignedUser(
            @PathVariable String userId, Pageable pageable) {

        return leadService.filterByAssignedUser(userId, pageable);
    }

    @PreAuthorize("hasAuthority('lead:update')")
    @PutMapping("/{leadId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update lead")
    public LeadResponse updateLead(
            @PathVariable String leadId,
            @Valid @RequestBody UpdateLeadRequest request) {

        return leadService.updateLead(leadId, request);
    }

    @PreAuthorize("hasAuthority('lead:convert')")
    @PostMapping("/{leadId}/convert")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Convert lead to client")
    public LeadResponse convertLeadToClient(
            @PathVariable String leadId) {

        return leadService.convertLeadToClient(leadId);
    }

    @PreAuthorize("hasAuthority('lead:delete')")
    @DeleteMapping("/{leadId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete lead")
    public void deleteLead(
            @PathVariable String leadId) {

        leadService.deleteLead(leadId);
    }
}
