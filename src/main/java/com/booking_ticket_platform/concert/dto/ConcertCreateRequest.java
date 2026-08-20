package com.booking_ticket_platform.concert.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class ConcertCreateRequest {
    @NotNull(message = "Venue ID is required")
    private UUID venueId;

    @NotBlank(message = "Concert name is required")
    private String name;

    @NotNull(message = "Event date is required")
    @Future(message = "Event date must be in the future")
    private OffsetDateTime eventDate;
    
    private String stageLayout;
}
