package com.viraj.dmabackend.client.exception;

import com.viraj.dmabackend.exception.ConflictException;

public class DuplicateClientEmailException extends ConflictException {

    public DuplicateClientEmailException(String email) {
        super("Client with email '" + email + "' already exists.");
    }
}
