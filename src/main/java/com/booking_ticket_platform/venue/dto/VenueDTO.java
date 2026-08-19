package com.booking_ticket_platform.venue.dto;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class VenueDTO {
    private UUID id;
    private String name;
    private String address;
    private String city;
    private int capacity;
}
