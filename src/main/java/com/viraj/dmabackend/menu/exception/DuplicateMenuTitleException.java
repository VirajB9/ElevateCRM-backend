package com.viraj.dmabackend.menu.exception;

import com.viraj.dmabackend.exception.BadRequestException;

public class DuplicateMenuTitleException extends BadRequestException {

    public DuplicateMenuTitleException(String title) {
        super("Menu with title '" + title + "' already exists.");
    }

}