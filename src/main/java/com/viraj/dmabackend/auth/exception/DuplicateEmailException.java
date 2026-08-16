package com.viraj.dmabackend.auth.exception;

import com.viraj.dmabackend.exception.BadRequestException;

public class DuplicateEmailException extends BadRequestException {

    public DuplicateEmailException(String email) {
        super("Email already exists: " + email);
    }
}
