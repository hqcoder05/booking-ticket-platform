package com.booking_ticket_platform.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class BookingRequest {

    @NotNull(message = "Concert ID is required")
    private UUID concertId;

    @NotNull(message = "Idempotency key is required")
    private String idempotencyKey;

    // For standing tickets
    private List<StandingTicketRequest> standingTickets;

    // For seated tickets
    private List<UUID> seatIds;

    @Data
    public static class StandingTicketRequest {
        @NotNull
        private UUID ticketCategoryId;
        private int quantity;
    }
}
