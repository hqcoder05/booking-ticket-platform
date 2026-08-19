package com.booking_ticket_platform.venue.service;

import com.booking_ticket_platform.venue.dto.VenueCreateRequest;
import com.booking_ticket_platform.venue.dto.VenueDTO;
import java.util.List;

public interface IVenueService {
    VenueDTO createVenue(VenueCreateRequest request);
    List<VenueDTO> getAllVenues();
}
