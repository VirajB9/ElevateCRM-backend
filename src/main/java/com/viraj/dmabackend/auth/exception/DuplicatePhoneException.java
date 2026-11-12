package com.viraj.dmabackend.auth.exception;

import com.viraj.dmabackend.exception.ConflictException;

public class DuplicatePhoneException extends ConflictException {

    public DuplicatePhoneException(String phoneNumber) {
        super("Phone number already exists: " + phoneNumber);
    }
}
