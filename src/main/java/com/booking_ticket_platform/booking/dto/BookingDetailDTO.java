package com.booking_ticket_platform.booking.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class BookingDetailDTO {
    private UUID id;
    private String categoryName;
    private String seatNumber;
    private int quantity;
    private BigDecimal price;
}