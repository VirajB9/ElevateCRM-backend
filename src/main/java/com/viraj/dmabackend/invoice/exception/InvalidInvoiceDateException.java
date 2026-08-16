package com.viraj.dmabackend.invoice.exception;

import com.viraj.dmabackend.exception.BadRequestException;

public class InvalidInvoiceDateException extends BadRequestException {

    public InvalidInvoiceDateException() {
        super("Invoice due date cannot be earlier than the issue date.");
    }
}