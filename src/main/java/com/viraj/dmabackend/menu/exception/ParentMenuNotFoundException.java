package com.viraj.dmabackend.menu.exception;

import com.viraj.dmabackend.exception.ResourceNotFoundException;

public class ParentMenuNotFoundException extends ResourceNotFoundException {

    public ParentMenuNotFoundException(String parentId) {
        super("Parent menu not found with id: " + parentId);
    }

}
