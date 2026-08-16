package com.viraj.dmabackend.auth.exception;

import com.viraj.dmabackend.auth.enums.UserStatus;
import com.viraj.dmabackend.exception.BadRequestException;

public class InvalidUserStatusException extends BadRequestException {

    public InvalidUserStatusException(UserStatus status) {
        super("Status '" + status + "' is not allowed for this operation.");
    }
}