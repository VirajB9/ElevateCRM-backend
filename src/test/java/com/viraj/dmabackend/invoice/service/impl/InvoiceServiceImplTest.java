package com.viraj.dmabackend.invoice.service.impl;

import com.viraj.dmabackend.invoice.entity.Invoice;
import com.viraj.dmabackend.invoice.entity.InvoiceItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class InvoiceServiceImplTest {

    @InjectMocks
    private InvoiceServiceImpl invoiceService;

    @Test
    void calculateInvoiceTotals_WithDiscountAndTax() throws Exception {
        Invoice invoice = new Invoice();
        invoice.setTaxPercentage(new BigDecimal("18.0"));
        invoice.setDiscount(new BigDecimal("2000.0"));
        
        InvoiceItem item = new InvoiceItem();
        item.setQuantity(new BigDecimal("1"));
        item.setUnitPrice(new BigDecimal("10000.0"));
        invoice.setItems(List.of(item));

        java.lang.reflect.Method method = InvoiceServiceImpl.class.getDeclaredMethod("calculateInvoiceTotals", Invoice.class);
        method.setAccessible(true);
        method.invoke(invoiceService, invoice);

        assertEquals(new BigDecimal("1440.00"), invoice.getTaxAmount());
        assertEquals(new BigDecimal("9440.00"), invoice.getTotalAmount());
        assertEquals(new BigDecimal("10000.00"), invoice.getSubtotal());
    }

    @Test
    void calculateInvoiceTotals_NoDiscount() throws Exception {
        Invoice invoice = new Invoice();
        invoice.setTaxPercentage(new BigDecimal("10.0"));
        
        InvoiceItem item = new InvoiceItem();
        item.setQuantity(new BigDecimal("1"));
        item.setUnitPrice(new BigDecimal("5000.0"));
        invoice.setItems(List.of(item));

        java.lang.reflect.Method method = InvoiceServiceImpl.class.getDeclaredMethod("calculateInvoiceTotals", Invoice.class);
        method.setAccessible(true);
        method.invoke(invoiceService, invoice);

        assertEquals(new BigDecimal("500.00"), invoice.getTaxAmount());
        assertEquals(new BigDecimal("5500.00"), invoice.getTotalAmount());
        assertEquals(new BigDecimal("0.00"), invoice.getDiscount());
    }

    @Test
    void calculateInvoiceTotals_100PercentDiscount() throws Exception {
        Invoice invoice = new Invoice();
        invoice.setTaxPercentage(new BigDecimal("18.0"));
        invoice.setDiscount(new BigDecimal("5000.0")); // Subtotal will be 5000, discount is 5000
        
        InvoiceItem item = new InvoiceItem();
        item.setQuantity(new BigDecimal("1"));
        item.setUnitPrice(new BigDecimal("5000.0"));
        invoice.setItems(List.of(item));

        java.lang.reflect.Method method = InvoiceServiceImpl.class.getDeclaredMethod("calculateInvoiceTotals", Invoice.class);
        method.setAccessible(true);
        method.invoke(invoiceService, invoice);

        assertEquals(new BigDecimal("0.00"), invoice.getTaxAmount());
        assertEquals(new BigDecimal("0.00"), invoice.getTotalAmount());
    }
}
