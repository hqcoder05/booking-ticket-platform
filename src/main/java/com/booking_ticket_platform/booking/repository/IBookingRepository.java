package com.booking_ticket_platform.booking.repository;

import com.booking_ticket_platform.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface IBookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findByUserId(UUID userId);
    Optional<Booking> findByIdempotencyKey(String idempotencyKey);

    @Query("SELECT b FROM Booking b WHERE b.status = 'PENDING' AND b.createdAt < :cutoffTime")
    List<Booking> findExpiredPendingBookings(@Param("cutoffTime") OffsetDateTime cutoffTime);

    @Query("SELECT b FROM Booking b WHERE (:status IS NULL OR b.status = :status) AND (:concertId IS NULL OR b.concert.id = :concertId)")
    List<Booking> findBookingsByFilters(@Param("status") String status, @Param("concertId") UUID concertId);

    List<Booking> findByConcertIdAndStatus(UUID concertId, String status);
}
