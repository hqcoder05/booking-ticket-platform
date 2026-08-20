package com.booking_ticket_platform.concert.repository;

import com.booking_ticket_platform.concert.entity.TicketCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface ITicketCategoryRepository extends JpaRepository<TicketCategory, UUID> {
    List<TicketCategory> findByConcertId(UUID concertId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TicketCategory t WHERE t.id = :id")
    Optional<TicketCategory> findByIdForUpdate(@Param("id") UUID id);
}
