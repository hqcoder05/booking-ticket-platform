package com.booking_ticket_platform.booking.repository;

import com.booking_ticket_platform.booking.entity.BookingDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IBookingDetailRepository extends JpaRepository<BookingDetail, UUID> {
}
