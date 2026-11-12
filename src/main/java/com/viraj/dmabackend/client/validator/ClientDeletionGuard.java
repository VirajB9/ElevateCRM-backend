package com.viraj.dmabackend.client.validator;

public interface ClientDeletionGuard {
    void validateCanArchiveClient(String clientId);
    void validateCanDeactivateClient(String clientId);
}
