package com.viraj.dmabackend.invoice.service;

import com.viraj.dmabackend.invoice.dto.*;
import com.viraj.dmabackend.invoice.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface InvoiceService {

    InvoiceResponse createInvoice(CreateInvoiceRequest request);

    InvoiceResponse getInvoiceById(String id);

    List<InvoiceHistoryResponse> getInvoiceHistory(String id);

    Page<InvoiceResponse> getAllInvoices(Pageable pageable);

    Page<InvoiceResponse> getInvoicesByStatus(InvoiceStatus status, Pageable pageable);

    Page<InvoiceResponse> getInvoicesByClientId(String clientId, Pageable pageable);

    Page<InvoiceResponse> getInvoicesByProjectId(String projectId, Pageable pageable);

    InvoiceResponse updateInvoice(String id, UpdateInvoiceRequest request);

    InvoiceResponse updateInvoiceStatus(String id, UpdateInvoiceStatusRequest request);

    void deleteInvoice(String id);
}
