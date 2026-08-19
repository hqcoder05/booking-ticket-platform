package com.booking_ticket_platform.venue.repository;

import com.booking_ticket_platform.venue.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface IVenueRepository extends JpaRepository<Venue, UUID> {
}
