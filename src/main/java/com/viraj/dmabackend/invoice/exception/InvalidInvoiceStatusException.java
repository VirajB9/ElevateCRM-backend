package com.viraj.dmabackend.invoice.exception;

import com.viraj.dmabackend.exception.BadRequestException;
import com.viraj.dmabackend.invoice.enums.InvoiceStatus;

public class InvalidInvoiceStatusException extends BadRequestException {

    public InvalidInvoiceStatusException(String message) {
        super(message);
    }

    public InvalidInvoiceStatusException(InvoiceStatus currentStatus, InvoiceStatus newStatus) {
        super(String.format("Cannot transition invoice status from %s to %s", currentStatus, newStatus));
    }
}
