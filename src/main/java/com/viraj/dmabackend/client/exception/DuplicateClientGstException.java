package com.viraj.dmabackend.client.exception;

import com.viraj.dmabackend.exception.ConflictException;

public class DuplicateClientGstException extends ConflictException {

    public DuplicateClientGstException(String gstNumber) {
        super("Client with GST number '" + gstNumber + "' already exists.");
    }
}
