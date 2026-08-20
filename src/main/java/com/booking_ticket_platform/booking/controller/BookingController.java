package com.booking_ticket_platform.booking.controller;

import com.booking_ticket_platform.booking.dto.BookingDTO;
import com.booking_ticket_platform.booking.dto.BookingRequest;
import com.booking_ticket_platform.booking.service.IBookingService;
import com.booking_ticket_platform.shared.dto.ApiResponse;
import com.booking_ticket_platform.shared.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/customer/bookings")
public class BookingController {

    private final IBookingService bookingService;

    public BookingController(IBookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BookingDTO>> createBooking(
            @Valid @RequestBody BookingRequest request,
            Authentication authentication) {
        
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UUID userId = userDetails.getUser().getId();
        
        BookingDTO booking = bookingService.createBooking(userId, request);
        return ResponseEntity.ok(ApiResponse.<BookingDTO>builder()
                .code(200)
                .message("Booking created successfully")
                .result(booking)
                .build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BookingDTO>>> getMyBookings(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UUID userId = userDetails.getUser().getId();
        
        List<BookingDTO> bookings = bookingService.getMyBookings(userId);
        return ResponseEntity.ok(ApiResponse.<List<BookingDTO>>builder()
                .code(200)
                .message("Success")
                .result(bookings)
                .build());
    }
}
