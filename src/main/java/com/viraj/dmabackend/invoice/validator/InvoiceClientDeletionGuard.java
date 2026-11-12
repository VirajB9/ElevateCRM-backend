package com.viraj.dmabackend.invoice.validator;

import com.viraj.dmabackend.client.validator.ClientDeletionGuard;
import com.viraj.dmabackend.exception.BadRequestException;
import com.viraj.dmabackend.invoice.enums.InvoiceStatus;
import com.viraj.dmabackend.invoice.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InvoiceClientDeletionGuard implements ClientDeletionGuard {

    private final InvoiceRepository invoiceRepository;

    @Override
    public void validateCanArchiveClient(String clientId) {
        String trimmedClientId = clientId != null ? clientId.trim() : null;
        if (trimmedClientId != null && invoiceRepository.existsByClientIdAndActiveTrueAndStatusIn(trimmedClientId,
                List.of(InvoiceStatus.DRAFT, InvoiceStatus.SENT, InvoiceStatus.OVERDUE))) {
            throw new BadRequestException("Cannot archive client with unpaid or active invoices.");
        }
    }

    @Override
    public void validateCanDeactivateClient(String clientId) {
        String trimmedClientId = clientId != null ? clientId.trim() : null;
        if (trimmedClientId != null && invoiceRepository.existsByClientIdAndActiveTrueAndStatusIn(trimmedClientId,
                List.of(InvoiceStatus.DRAFT, InvoiceStatus.SENT, InvoiceStatus.OVERDUE))) {
            throw new BadRequestException("Cannot deactivate client with unpaid or active invoices.");
        }
    }
}
