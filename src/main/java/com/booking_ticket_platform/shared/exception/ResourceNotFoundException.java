package com.booking_ticket_platform.shared.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BusinessException {



    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }


    
    public ResourceNotFoundException(String resourceName, Object id) {
        super(resourceName + " khong the tim thay voi ID: " + id, HttpStatus.NOT_FOUND);
    }
}
