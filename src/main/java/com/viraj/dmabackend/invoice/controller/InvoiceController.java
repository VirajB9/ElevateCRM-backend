package com.viraj.dmabackend.invoice.controller;

import com.viraj.dmabackend.invoice.dto.CreateInvoiceRequest;
import com.viraj.dmabackend.invoice.dto.InvoiceResponse;
import com.viraj.dmabackend.invoice.dto.UpdateInvoiceRequest;
import com.viraj.dmabackend.invoice.dto.UpdateInvoiceStatusRequest;
import com.viraj.dmabackend.invoice.enums.InvoiceStatus;
import com.viraj.dmabackend.invoice.service.InvoiceService;
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

@Tag(name = "9. Invoice Management")
@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PreAuthorize("hasAuthority('invoice:create')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create Invoice")
    public InvoiceResponse createInvoice(
            @Valid @RequestBody CreateInvoiceRequest request) {

        return invoiceService.createInvoice(request);
    }

    @PreAuthorize("hasAuthority('invoice:read')")
    @GetMapping("/{invoiceId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get Invoice By ID")
    public InvoiceResponse getInvoiceById(
            @PathVariable String invoiceId) {

        return invoiceService.getInvoiceById(invoiceId);
    }

    @PreAuthorize("hasAuthority('invoice:read')")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get All Invoices")
    public Page<InvoiceResponse> getAllInvoices(
            Pageable pageable) {

        return invoiceService.getAllInvoices(pageable);
    }

    @PreAuthorize("hasAuthority('invoice:read')")
    @GetMapping("/status/{status}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get Invoices By Status")
    public Page<InvoiceResponse> getInvoicesByStatus(
            @PathVariable InvoiceStatus status, Pageable pageable) {

        return invoiceService.getInvoicesByStatus(status, pageable);
    }

    @PreAuthorize("hasAuthority('invoice:read')")
    @GetMapping("/client/{clientId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get Invoices By Client")
    public Page<InvoiceResponse> getInvoicesByClientId(
            @PathVariable String clientId, Pageable pageable) {

        return invoiceService.getInvoicesByClientId(clientId, pageable);
    }

    @PreAuthorize("hasAuthority('invoice:read')")
    @GetMapping("/project/{projectId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get Invoices By Project")
    public Page<InvoiceResponse> getInvoicesByProjectId(
            @PathVariable String projectId, Pageable pageable) {

        return invoiceService.getInvoicesByProjectId(projectId, pageable);
    }

    @PreAuthorize("hasAuthority('invoice:update')")
    @PutMapping("/{invoiceId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update Invoice")
    public InvoiceResponse updateInvoice(
            @PathVariable String invoiceId,
            @Valid @RequestBody UpdateInvoiceRequest request) {

        return invoiceService.updateInvoice(invoiceId, request);
    }

    @PreAuthorize("hasAnyAuthority('invoice:update', 'invoice:pay')")
    @PatchMapping("/{invoiceId}/status")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update Invoice Status")
    public InvoiceResponse updateInvoiceStatus(
            @PathVariable String invoiceId,
            @Valid @RequestBody UpdateInvoiceStatusRequest request) {

        return invoiceService.updateInvoiceStatus(invoiceId, request);
    }

    @PreAuthorize("hasAuthority('invoice:delete')")
    @DeleteMapping("/{invoiceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete Invoice")
    public void deleteInvoice(
            @PathVariable String invoiceId) {

        invoiceService.deleteInvoice(invoiceId);
    }
}