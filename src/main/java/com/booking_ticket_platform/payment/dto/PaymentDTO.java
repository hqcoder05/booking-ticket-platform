package com.booking_ticket_platform.payment.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class PaymentDTO {
    private UUID id;
    private UUID bookingId;
    private String status;
    private String method;
}
