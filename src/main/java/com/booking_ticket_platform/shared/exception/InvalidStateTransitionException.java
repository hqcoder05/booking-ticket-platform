package com.booking_ticket_platform.shared.exception;

import org.springframework.http.HttpStatus;

public class InvalidStateTransitionException extends BusinessException {



    public InvalidStateTransitionException(String message) {
        super(message, HttpStatus.CONFLICT);
    }


    
}
