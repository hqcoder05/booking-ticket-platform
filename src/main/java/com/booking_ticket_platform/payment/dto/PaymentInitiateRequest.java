package com.booking_ticket_platform.payment.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class PaymentInitiateRequest {
    private UUID bookingId;
    private String method;
}
