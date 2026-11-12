package com.viraj.dmabackend.auth.exception;

import com.viraj.dmabackend.exception.ConflictException;

public class DuplicateEmailException extends ConflictException {

    public DuplicateEmailException(String email) {
        super("Email already exists: " + email);
    }
}
