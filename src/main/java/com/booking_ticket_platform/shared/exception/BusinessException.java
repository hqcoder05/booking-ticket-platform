package com.booking_ticket_platform.shared.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {



    private final HttpStatus status;




    public BusinessException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }



    public HttpStatus getStatus() {
        return status;
    }


    
}
