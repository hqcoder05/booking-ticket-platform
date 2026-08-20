package com.booking_ticket_platform.concert.dto;

import com.booking_ticket_platform.venue.dto.VenueDTO;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ConcertDTO {
    private UUID id;
    private VenueDTO venue;
    private String name;
    private OffsetDateTime eventDate;
    private String status;
    private String stageLayout;
    private List<TicketCategoryDTO> ticketCategories;
}
