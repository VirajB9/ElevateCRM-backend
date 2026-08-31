package com.viraj.dmabackend.invoice.controller;

import com.viraj.dmabackend.invoice.dto.CreateInvoiceRequest;
import com.viraj.dmabackend.invoice.dto.InvoiceResponse;
import com.viraj.dmabackend.invoice.dto.UpdateInvoiceRequest;
import com.viraj.dmabackend.invoice.dto.UpdateInvoiceStatusRequest;
import com.viraj.dmabackend.invoice.enums.InvoiceStatus;
import com.viraj.dmabackend.invoice.service.InvoiceService;
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

@Tag(name = "9. Invoice Management", description = "Endpoints for managing client invoices and billing")
@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PreAuthorize("hasAuthority('invoice:create')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create Invoice", description = "Creates a new invoice for a client/project. Requires 'invoice:create' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Invoice created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public InvoiceResponse createInvoice(
            @Valid @RequestBody CreateInvoiceRequest request) {
        return invoiceService.createInvoice(request);
    }

    @PreAuthorize("hasAuthority('invoice:read')")
    @GetMapping("/{invoiceId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get Invoice By ID", description = "Retrieves a specific invoice by ID. Requires 'invoice:read' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Invoice retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Invoice not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public InvoiceResponse getInvoiceById(
            @Parameter(description = "Unique ID of the invoice", required = true) @PathVariable String invoiceId) {
        return invoiceService.getInvoiceById(invoiceId);
    }

    @PreAuthorize("hasAuthority('invoice:read')")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get All Invoices", description = "Retrieves a paginated list of all invoices. Requires 'invoice:read' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Invoices retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public Page<InvoiceResponse> getAllInvoices(
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        return invoiceService.getAllInvoices(pageable);
    }

    @PreAuthorize("hasAuthority('invoice:read')")
    @GetMapping("/status/{status}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get Invoices By Status", description = "Retrieves invoices filtered by their status. Requires 'invoice:read' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Filtered invoices returned successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public Page<InvoiceResponse> getInvoicesByStatus(
            @Parameter(description = "Invoice status", required = true) @PathVariable InvoiceStatus status,
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        return invoiceService.getInvoicesByStatus(status, pageable);
    }

    @PreAuthorize("hasAuthority('invoice:read')")
    @GetMapping("/client/{clientId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get Invoices By Client", description = "Retrieves invoices associated with a specific client. Requires 'invoice:read' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Filtered invoices returned successfully"),
            @ApiResponse(responseCode = "404", description = "Client not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public Page<InvoiceResponse> getInvoicesByClientId(
            @Parameter(description = "Unique ID of the client", required = true) @PathVariable String clientId,
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        return invoiceService.getInvoicesByClientId(clientId, pageable);
    }

    @PreAuthorize("hasAuthority('invoice:read')")
    @GetMapping("/project/{projectId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get Invoices By Project", description = "Retrieves invoices associated with a specific project. Requires 'invoice:read' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Filtered invoices returned successfully"),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public Page<InvoiceResponse> getInvoicesByProjectId(
            @Parameter(description = "Unique ID of the project", required = true) @PathVariable String projectId,
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        return invoiceService.getInvoicesByProjectId(projectId, pageable);
    }

    @PreAuthorize("hasAuthority('invoice:update')")
    @PutMapping("/{invoiceId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update Invoice", description = "Updates an invoice's details and line items. Requires 'invoice:update' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Invoice updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Invoice not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public InvoiceResponse updateInvoice(
            @Parameter(description = "Unique ID of the invoice", required = true) @PathVariable String invoiceId,
            @Valid @RequestBody UpdateInvoiceRequest request) {
        return invoiceService.updateInvoice(invoiceId, request);
    }

    @PreAuthorize("hasAnyAuthority('invoice:update', 'invoice:pay')")
    @PatchMapping("/{invoiceId}/status")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update Invoice Status", description = "Updates only the status of an invoice. Requires 'invoice:update' or 'invoice:pay' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Invoice status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Invoice not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public InvoiceResponse updateInvoiceStatus(
            @Parameter(description = "Unique ID of the invoice", required = true) @PathVariable String invoiceId,
            @Valid @RequestBody UpdateInvoiceStatusRequest request) {
        return invoiceService.updateInvoiceStatus(invoiceId, request);
    }

    @PreAuthorize("hasAuthority('invoice:delete')")
    @DeleteMapping("/{invoiceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete Invoice", description = "Deletes or archives an invoice. Requires 'invoice:delete' authority.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Invoice successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Invoice not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public void deleteInvoice(
            @Parameter(description = "Unique ID of the invoice", required = true) @PathVariable String invoiceId) {
        invoiceService.deleteInvoice(invoiceId);
    }
}
