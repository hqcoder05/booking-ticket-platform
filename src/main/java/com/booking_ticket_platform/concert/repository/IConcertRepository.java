package com.booking_ticket_platform.concert.repository;

import com.booking_ticket_platform.concert.entity.Concert;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface IConcertRepository extends JpaRepository<Concert, UUID> {
    List<Concert> findByStatus(String status);
}
