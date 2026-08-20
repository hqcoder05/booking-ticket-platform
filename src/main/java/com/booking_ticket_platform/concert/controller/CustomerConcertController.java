package com.booking_ticket_platform.concert.controller;

import com.booking_ticket_platform.concert.dto.ConcertDTO;
import com.booking_ticket_platform.concert.service.IConcertService;
import com.booking_ticket_platform.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/customer/concerts")
@Tag(name = "Customer - Concert", description = "Endpoints for viewing published concerts")
public class CustomerConcertController {

    private final IConcertService concertService;

    public CustomerConcertController(IConcertService concertService) {
        this.concertService = concertService;
    }

    @GetMapping
    @Operation(summary = "Get all published concerts")
    public ResponseEntity<ApiResponse<List<ConcertDTO>>> getPublishedConcerts() {
        List<ConcertDTO> concerts = concertService.getPublishedConcerts();
        return ResponseEntity.ok(ApiResponse.<List<ConcertDTO>>builder()
                .code(200)
                .message("Fetched published concerts successfully")
                .result(concerts)
                .timestamp(OffsetDateTime.now())
                .build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get concert details by ID")
    public ResponseEntity<ApiResponse<ConcertDTO>> getConcertById(@PathVariable UUID id) {
        ConcertDTO concert = concertService.getConcertById(id);
        return ResponseEntity.ok(ApiResponse.<ConcertDTO>builder()
                .code(200)
                .message("Fetched concert successfully")
                .result(concert)
                .timestamp(OffsetDateTime.now())
                .build());
    }

    @GetMapping("/{id}/seats")
    @Operation(summary = "Get seats for a concert")
    public ResponseEntity<ApiResponse<List<com.booking_ticket_platform.concert.dto.SeatDTO>>> getConcertSeats(@PathVariable UUID id) {
        List<com.booking_ticket_platform.concert.dto.SeatDTO> seats = concertService.getConcertSeats(id);
        return ResponseEntity.ok(ApiResponse.<List<com.booking_ticket_platform.concert.dto.SeatDTO>>builder()
                .code(200)
                .message("Fetched seats successfully")
                .result(seats)
                .timestamp(OffsetDateTime.now())
                .build());
    }
}
