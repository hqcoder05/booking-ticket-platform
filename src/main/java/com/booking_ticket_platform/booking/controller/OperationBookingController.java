package com.booking_ticket_platform.booking.controller;

import com.booking_ticket_platform.booking.dto.BookingDTO;
import com.booking_ticket_platform.booking.service.IBookingService;
import com.booking_ticket_platform.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/operation/bookings")
@Tag(name = "Operation - Booking", description = "Endpoints for managing bookings by Admin/Operator")
public class OperationBookingController {

    private final IBookingService bookingService;

    public OperationBookingController(IBookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    @Operation(summary = "Get all bookings with optional filters")
    public ResponseEntity<ApiResponse<List<BookingDTO>>> getAllBookings(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID concertId) {
        List<BookingDTO> bookings = bookingService.getAllBookingsForOperation(status, concertId);
        return ResponseEntity.ok(ApiResponse.<List<BookingDTO>>builder()
                .code(200)
                .message("Fetched bookings successfully")
                .result(bookings)
                .build());
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Manually update a booking status")
    public ResponseEntity<ApiResponse<BookingDTO>> updateBookingStatus(
            @PathVariable UUID id,
            @RequestParam String status) {
        BookingDTO booking = bookingService.updateBookingStatus(id, status);
        return ResponseEntity.ok(ApiResponse.<BookingDTO>builder()
                .code(200)
                .message("Booking status updated")
                .result(booking)
                .build());
    }
}
