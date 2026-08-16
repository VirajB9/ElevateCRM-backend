package com.viraj.dmabackend.menu.exception;

import com.viraj.dmabackend.exception.BadRequestException;

public class DuplicateMenuPathException extends BadRequestException {

    public DuplicateMenuPathException(String path) {
        super("Menu with path '" + path + "' already exists.");
    }
}
