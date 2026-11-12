package com.viraj.dmabackend.menu.exception;

import com.viraj.dmabackend.exception.ConflictException;

public class DuplicateMenuPathException extends ConflictException {

    public DuplicateMenuPathException(String path) {
        super("Menu with path '" + path + "' already exists.");
    }
}
