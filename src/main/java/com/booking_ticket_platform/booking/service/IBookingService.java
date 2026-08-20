package com.booking_ticket_platform.booking.service;

import com.booking_ticket_platform.booking.dto.BookingDTO;
import com.booking_ticket_platform.booking.dto.BookingRequest;

import java.util.List;
import java.util.UUID;

public interface IBookingService {
    BookingDTO createBooking(UUID userId, BookingRequest request);
    List<BookingDTO> getMyBookings(UUID userId);
}
