package com.viraj.dmabackend.client.exception;

import com.viraj.dmabackend.exception.BadRequestException;

public class DuplicateClientEmailException extends BadRequestException {

    public DuplicateClientEmailException(String email) {
        super("Client with email '" + email + "' already exists.");
    }
}