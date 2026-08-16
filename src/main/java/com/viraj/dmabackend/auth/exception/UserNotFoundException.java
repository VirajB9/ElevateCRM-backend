package com.viraj.dmabackend.auth.exception;

import com.viraj.dmabackend.exception.ResourceNotFoundException;

public class UserNotFoundException extends ResourceNotFoundException {

    public UserNotFoundException(String userId) {
        super("User not found with id: " + userId);
    }
}