package com.viraj.dmabackend.auth.exception;

import com.viraj.dmabackend.exception.BadRequestException;

public class DuplicatePhoneException extends BadRequestException {

    public DuplicatePhoneException(String phoneNumber) {
        super("Phone number already exists: " + phoneNumber);
    }
}
