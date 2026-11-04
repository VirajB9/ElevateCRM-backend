package com.viraj.dmabackend.invoice.service.impl;

import com.viraj.dmabackend.client.repository.ClientRepository;
import com.viraj.dmabackend.invoice.entity.Invoice;
import com.viraj.dmabackend.invoice.entity.InvoiceItem;
import com.viraj.dmabackend.invoice.enums.InvoiceStatus;
import com.viraj.dmabackend.invoice.exception.InvalidInvoiceStatusException;
import com.viraj.dmabackend.invoice.mapper.InvoiceMapper;
import com.viraj.dmabackend.invoice.repository.InvoiceHistoryRepository;
import com.viraj.dmabackend.invoice.repository.InvoiceRepository;
import com.viraj.dmabackend.invoice.validator.InvoiceValidator;
import com.viraj.dmabackend.project.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InvoiceServiceImplTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private InvoiceHistoryRepository invoiceHistoryRepository;

    @Mock
    private InvoiceMapper invoiceMapper;

    @Mock
    private InvoiceValidator invoiceValidator;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private InvoiceServiceImpl invoiceService;

    @Test
    void calculateInvoiceTotals_WithDiscountAndTax() {
        Invoice invoice = new Invoice();
        invoice.setTaxPercentage(new BigDecimal("18.0"));
        invoice.setDiscount(new BigDecimal("2000.0"));
        
        InvoiceItem item = new InvoiceItem();
        item.setQuantity(new BigDecimal("1"));
        item.setUnitPrice(new BigDecimal("10000.0"));
        invoice.setItems(List.of(item));

        invoiceService.calculateInvoiceTotals(invoice);

        assertEquals(new BigDecimal("1440.00"), invoice.getTaxAmount());
        assertEquals(new BigDecimal("9440.00"), invoice.getTotalAmount());
        assertEquals(new BigDecimal("10000.00"), invoice.getSubtotal());
    }

    @Test
    void calculateInvoiceTotals_NoDiscount() {
        Invoice invoice = new Invoice();
        invoice.setTaxPercentage(new BigDecimal("10.0"));
        
        InvoiceItem item = new InvoiceItem();
        item.setQuantity(new BigDecimal("1"));
        item.setUnitPrice(new BigDecimal("5000.0"));
        invoice.setItems(List.of(item));

        invoiceService.calculateInvoiceTotals(invoice);

        assertEquals(new BigDecimal("500.00"), invoice.getTaxAmount());
        assertEquals(new BigDecimal("5500.00"), invoice.getTotalAmount());
        assertEquals(new BigDecimal("0.00"), invoice.getDiscount());
    }

    @Test
    void calculateInvoiceTotals_100PercentDiscount() {
        Invoice invoice = new Invoice();
        invoice.setTaxPercentage(new BigDecimal("18.0"));
        invoice.setDiscount(new BigDecimal("5000.0")); // Subtotal will be 5000, discount is 5000
        
        InvoiceItem item = new InvoiceItem();
        item.setQuantity(new BigDecimal("1"));
        item.setUnitPrice(new BigDecimal("5000.0"));
        invoice.setItems(List.of(item));

        invoiceService.calculateInvoiceTotals(invoice);

        assertEquals(new BigDecimal("0.00"), invoice.getTaxAmount());
        assertEquals(new BigDecimal("0.00"), invoice.getTotalAmount());
    }

    @Test
    void deleteInvoice_ThrowsWhenStatusIsPaid() {
        Invoice invoice = new Invoice();
        invoice.setId("inv-123");
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setActive(true);

        when(invoiceRepository.findByIdAndActiveTrue("inv-123")).thenReturn(Optional.of(invoice));

        assertThrows(InvalidInvoiceStatusException.class, () -> invoiceService.deleteInvoice("inv-123"));
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void deleteInvoice_SuccessWhenStatusIsDraft() {
        Invoice invoice = new Invoice();
        invoice.setId("inv-123");
        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice.setActive(true);

        when(invoiceRepository.findByIdAndActiveTrue("inv-123")).thenReturn(Optional.of(invoice));

        invoiceService.deleteInvoice("inv-123");

        assertFalse(invoice.getActive());
        assertTrue(invoice.isDeleted());
        verify(invoiceRepository).save(invoice);
    }
}
