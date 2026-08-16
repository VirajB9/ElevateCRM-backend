package com.viraj.dmabackend.menu.exception;

import com.viraj.dmabackend.exception.BadRequestException;

public class InvalidMenuPathException extends BadRequestException {

    public InvalidMenuPathException(String path) {
        super("Invalid menu path: '" + path + "'. Path must start with '/'.");
    }

}