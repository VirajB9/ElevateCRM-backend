package com.viraj.dmabackend.menu.exception;

import com.viraj.dmabackend.exception.BadRequestException;

public class SelfParentMenuException extends BadRequestException {

    public SelfParentMenuException() {
        super("A menu cannot be its own parent.");
    }

}