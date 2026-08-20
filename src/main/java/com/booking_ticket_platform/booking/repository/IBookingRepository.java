package com.booking_ticket_platform.booking.repository;

import com.booking_ticket_platform.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IBookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findByUserId(UUID userId);
    Optional<Booking> findByIdempotencyKey(String idempotencyKey);
}
