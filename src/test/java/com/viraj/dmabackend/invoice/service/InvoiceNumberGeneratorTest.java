package com.viraj.dmabackend.invoice.service;

import com.viraj.dmabackend.common.counter.CounterRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceNumberGeneratorTest {

    @Mock
    private CounterRepository counterRepository;

    @InjectMocks
    private InvoiceNumberGenerator invoiceNumberGenerator;

    @Test
    void generate_FormatsInvoiceNumberWithUtcYearAndPaddedSequence_WhenNoDateProvided() {
        int expectedYear = Year.now(ZoneOffset.UTC).getValue();
        String counterKey = "invoice_" + expectedYear;
        when(counterRepository.getNextSequence(counterKey)).thenReturn(42L);

        String result = invoiceNumberGenerator.generate();

        String expectedInvoiceNumber = String.format("INV-%d-00042", expectedYear);
        assertEquals(expectedInvoiceNumber, result);
        verify(counterRepository).getNextSequence(counterKey);
    }

    @Test
    void generate_FormatsInvoiceNumberWithIssueDateYear_WhenIssueDateProvided() {
        LocalDate issueDate = LocalDate.of(2027, 1, 1);
        String counterKey = "invoice_2027";
        when(counterRepository.getNextSequence(counterKey)).thenReturn(105L);

        String result = invoiceNumberGenerator.generate(issueDate);

        assertEquals("INV-2027-00105", result);
        verify(counterRepository).getNextSequence(counterKey);
    }

    @Test
    void compensate_DecrementsSequenceWhenInvoiceNumberValid() {
        invoiceNumberGenerator.compensate("INV-2027-00105");

        verify(counterRepository).decrementSequenceIfCurrent("invoice_2027", 105L);
    }
}
