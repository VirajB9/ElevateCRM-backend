package com.viraj.dmabackend.invoice.exception;

import com.viraj.dmabackend.exception.ResourceNotFoundException;

public class InvoiceNotFoundException extends ResourceNotFoundException {

    public InvoiceNotFoundException(String invoiceId) {
        super("Invoice not found with id: " + invoiceId);
    }
}