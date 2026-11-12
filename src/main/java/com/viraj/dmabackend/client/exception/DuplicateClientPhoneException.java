package com.viraj.dmabackend.client.exception;

import com.viraj.dmabackend.exception.ConflictException;

public class DuplicateClientPhoneException extends ConflictException {

    public DuplicateClientPhoneException(String phoneNumber) {
        super("Client with phone number '" + phoneNumber + "' already exists.");
    }
}
