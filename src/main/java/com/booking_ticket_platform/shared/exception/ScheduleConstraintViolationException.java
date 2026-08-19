package com.booking_ticket_platform.shared.exception;

import org.springframework.http.HttpStatus;

public class ScheduleConstraintViolationException extends BusinessException {



    public ScheduleConstraintViolationException(String message) {
        super(message, HttpStatus.UNPROCESSABLE_CONTENT);
    }   


    
}
