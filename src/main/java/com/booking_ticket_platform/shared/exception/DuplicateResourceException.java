package com.booking_ticket_platform.shared.exception;

import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends BusinessException {




    public DuplicateResourceException(String message) {
        super(message, HttpStatus.CONFLICT);
    }



}
