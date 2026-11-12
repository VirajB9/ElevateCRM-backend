package com.viraj.dmabackend.menu.exception;

import com.viraj.dmabackend.exception.ConflictException;

public class DuplicateMenuTitleException extends ConflictException {

    public DuplicateMenuTitleException(String title) {
        super("Menu with title '" + title + "' already exists.");
    }

}
