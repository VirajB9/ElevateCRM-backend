package com.viraj.dmabackend.menu.exception;

import com.viraj.dmabackend.exception.ResourceNotFoundException;

public class MenuNotFoundException extends ResourceNotFoundException {

    public MenuNotFoundException(String menuId) {
        super("Menu not found with id: " + menuId);
    }

}
