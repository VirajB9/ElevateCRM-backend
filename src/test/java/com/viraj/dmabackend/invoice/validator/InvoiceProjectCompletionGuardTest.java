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
class InvoiceProjectCompletionGuardTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private InvoiceProjectCompletionGuard guard;

    @Test
    void validateCanCompleteProject_whenActiveInvoicesExist_throwsBadRequest() {
        when(invoiceRepository.existsByProjectIdAndActiveTrueAndStatusIn(eq("proj-1"), eq(List.of(
                InvoiceStatus.DRAFT, InvoiceStatus.SENT, InvoiceStatus.OVERDUE))))
                .thenReturn(true);

        assertThrows(BadRequestException.class, () -> guard.validateCanCompleteProject("  proj-1  "));
    }

    @Test
    void validateCanCompleteProject_whenNoActiveInvoices_passes() {
        when(invoiceRepository.existsByProjectIdAndActiveTrueAndStatusIn(eq("proj-1"), eq(List.of(
                InvoiceStatus.DRAFT, InvoiceStatus.SENT, InvoiceStatus.OVERDUE))))
                .thenReturn(false);

        assertDoesNotThrow(() -> guard.validateCanCompleteProject("proj-1"));
        verify(invoiceRepository).existsByProjectIdAndActiveTrueAndStatusIn(eq("proj-1"), eq(List.of(
                InvoiceStatus.DRAFT, InvoiceStatus.SENT, InvoiceStatus.OVERDUE)));
    }

    @Test
    void validateCanCompleteProject_whenProjectIdIsNull_passes() {
        assertDoesNotThrow(() -> guard.validateCanCompleteProject(null));
    }
}
