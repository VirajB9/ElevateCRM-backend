package com.viraj.dmabackend.client.exception;

import com.viraj.dmabackend.exception.BadRequestException;

public class DuplicateClientGstException extends BadRequestException {

    public DuplicateClientGstException(String gstNumber) {
        super("Client with GST number '" + gstNumber + "' already exists.");
    }
}
