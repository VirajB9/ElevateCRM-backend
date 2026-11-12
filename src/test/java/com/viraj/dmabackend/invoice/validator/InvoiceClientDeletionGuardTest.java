package com.viraj.dmabackend.invoice.validator;

import com.viraj.dmabackend.exception.BadRequestException;
import com.viraj.dmabackend.invoice.enums.InvoiceStatus;
import com.viraj.dmabackend.invoice.repository.InvoiceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceClientDeletionGuardTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private InvoiceClientDeletionGuard guard;

    @Test
    void validateCanArchiveClient_whenActiveInvoicesExist_throwsBadRequest() {
        when(invoiceRepository.existsByClientIdAndActiveTrueAndStatusIn(eq("client-1"), eq(List.of(
                InvoiceStatus.DRAFT, InvoiceStatus.SENT, InvoiceStatus.OVERDUE))))
                .thenReturn(true);

        assertThrows(BadRequestException.class, () -> guard.validateCanArchiveClient("  client-1  "));
    }

    @Test
    void validateCanArchiveClient_whenNoActiveInvoices_passes() {
        when(invoiceRepository.existsByClientIdAndActiveTrueAndStatusIn(eq("client-1"), eq(List.of(
                InvoiceStatus.DRAFT, InvoiceStatus.SENT, InvoiceStatus.OVERDUE))))
                .thenReturn(false);

        assertDoesNotThrow(() -> guard.validateCanArchiveClient("client-1"));
        verify(invoiceRepository).existsByClientIdAndActiveTrueAndStatusIn(eq("client-1"), eq(List.of(
                InvoiceStatus.DRAFT, InvoiceStatus.SENT, InvoiceStatus.OVERDUE)));
    }

    @Test
    void validateCanDeactivateClient_whenActiveInvoicesExist_throwsBadRequest() {
        when(invoiceRepository.existsByClientIdAndActiveTrueAndStatusIn(eq("client-1"), eq(List.of(
                InvoiceStatus.DRAFT, InvoiceStatus.SENT, InvoiceStatus.OVERDUE))))
                .thenReturn(true);

        assertThrows(BadRequestException.class, () -> guard.validateCanDeactivateClient("  client-1  "));
    }

    @Test
    void validateCanDeactivateClient_whenNoActiveInvoices_passes() {
        when(invoiceRepository.existsByClientIdAndActiveTrueAndStatusIn(eq("client-1"), eq(List.of(
                InvoiceStatus.DRAFT, InvoiceStatus.SENT, InvoiceStatus.OVERDUE))))
                .thenReturn(false);

        assertDoesNotThrow(() -> guard.validateCanDeactivateClient("client-1"));
    }

    @Test
    void validateCanArchiveClient_whenClientIdIsNull_passes() {
        assertDoesNotThrow(() -> guard.validateCanArchiveClient(null));
    }
}
