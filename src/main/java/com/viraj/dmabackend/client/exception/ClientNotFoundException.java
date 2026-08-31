package com.viraj.dmabackend.client.exception;

import com.viraj.dmabackend.exception.ResourceNotFoundException;

public class ClientNotFoundException extends ResourceNotFoundException {

    public ClientNotFoundException(String clientId) {
        super("Client not found with id: " + clientId);
    }
}
