package com.viraj.dmabackend.auth.validator;

public interface UserDeletionGuard {
    void validateCanDeleteUser(String userId);
}
