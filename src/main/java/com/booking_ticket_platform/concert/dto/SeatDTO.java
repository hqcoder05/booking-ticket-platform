package com.booking_ticket_platform.concert.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class SeatDTO {
    private UUID id;
    private UUID ticketCategoryId;
    private String seatNumber;
    private String status;
}
