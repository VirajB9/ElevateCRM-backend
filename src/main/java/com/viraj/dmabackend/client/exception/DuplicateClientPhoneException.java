package com.viraj.dmabackend.client.exception;

import com.viraj.dmabackend.exception.BadRequestException;

public class DuplicateClientPhoneException extends BadRequestException {

    public DuplicateClientPhoneException(String phoneNumber) {
        super("Client with phone number '" + phoneNumber + "' already exists.");
    }
}