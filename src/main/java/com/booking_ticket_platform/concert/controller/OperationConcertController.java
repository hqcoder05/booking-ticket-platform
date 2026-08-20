package com.booking_ticket_platform.concert.controller;

import com.booking_ticket_platform.concert.dto.ConcertCreateRequest;
import com.booking_ticket_platform.concert.dto.ConcertDTO;
import com.booking_ticket_platform.concert.dto.TicketCategoryCreateRequest;
import com.booking_ticket_platform.concert.dto.TicketCategoryDTO;
import com.booking_ticket_platform.concert.service.IConcertService;
import com.booking_ticket_platform.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/operation/concerts")
@Tag(name = "Operation - Concert", description = "Endpoints for managing concerts by Admin/Operator")
public class OperationConcertController {

    private final IConcertService concertService;

    public OperationConcertController(IConcertService concertService) {
        this.concertService = concertService;
    }

    @GetMapping
    @Operation(summary = "Get list of all concerts (including DRAFT, CANCELLED)")
    public ResponseEntity<ApiResponse<java.util.List<ConcertDTO>>> getAllConcerts() {
        java.util.List<ConcertDTO> concerts = concertService.getAllConcerts();
        return ResponseEntity.ok(ApiResponse.<java.util.List<ConcertDTO>>builder()
                .code(200)
                .message("Fetched all concerts successfully")
                .result(concerts)
                .timestamp(OffsetDateTime.now())
                .build());
    }

    @PostMapping
    @Operation(summary = "Create a new concert (DRAFT)")
    public ResponseEntity<ApiResponse<ConcertDTO>> createConcert(@Valid @RequestBody ConcertCreateRequest request) {
        ConcertDTO concert = concertService.createConcert(request);
        return ResponseEntity.ok(ApiResponse.<ConcertDTO>builder()
                .code(200)
                .message("Concert created successfully")
                .result(concert)
                .timestamp(OffsetDateTime.now())
                .build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing concert")
    public ResponseEntity<ApiResponse<ConcertDTO>> updateConcert(
            @PathVariable UUID id,
            @Valid @RequestBody ConcertCreateRequest request) {
        ConcertDTO concert = concertService.updateConcert(id, request);
        return ResponseEntity.ok(ApiResponse.<ConcertDTO>builder()
                .code(200)
                .message("Concert updated successfully")
                .result(concert)
                .timestamp(OffsetDateTime.now())
                .build());
    }

    @PutMapping("/{id}/publish")
    @Operation(summary = "Publish a concert (DRAFT -> PUBLISHED)")
    public ResponseEntity<ApiResponse<ConcertDTO>> publishConcert(@PathVariable UUID id) {
        ConcertDTO concert = concertService.publishConcert(id);
        return ResponseEntity.ok(ApiResponse.<ConcertDTO>builder()
                .code(200)
                .message("Concert published successfully")
                .result(concert)
                .timestamp(OffsetDateTime.now())
                .build());
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel a concert and automatically refund completed bookings")
    public ResponseEntity<ApiResponse<ConcertDTO>> cancelConcert(@PathVariable UUID id) {
        ConcertDTO concert = concertService.cancelConcert(id);
        return ResponseEntity.ok(ApiResponse.<ConcertDTO>builder()
                .code(200)
                .message("Concert cancelled and bookings refunded")
                .result(concert)
                .timestamp(OffsetDateTime.now())
                .build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a DRAFT concert")
    public ResponseEntity<ApiResponse<Void>> deleteConcert(@PathVariable UUID id) {
        concertService.deleteConcert(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(200)
                .message("Concert deleted successfully")
                .timestamp(OffsetDateTime.now())
                .build());
    }

    @PostMapping("/{id}/ticket-categories")
    @Operation(summary = "Add a ticket category to a concert")
    public ResponseEntity<ApiResponse<TicketCategoryDTO>> addTicketCategory(
            @PathVariable UUID id,
            @Valid @RequestBody TicketCategoryCreateRequest request) {
        TicketCategoryDTO category = concertService.addTicketCategory(id, request);
        return ResponseEntity.ok(ApiResponse.<TicketCategoryDTO>builder()
                .code(200)
                .message("Ticket category added successfully")
                .result(category)
                .timestamp(OffsetDateTime.now())
                .build());
    }

    @PutMapping("/{id}/ticket-categories/{categoryId}")
    @Operation(summary = "Update a ticket category")
    public ResponseEntity<ApiResponse<TicketCategoryDTO>> updateTicketCategory(
            @PathVariable UUID id,
            @PathVariable UUID categoryId,
            @Valid @RequestBody TicketCategoryCreateRequest request) {
        TicketCategoryDTO category = concertService.updateTicketCategory(id, categoryId, request);
        return ResponseEntity.ok(ApiResponse.<TicketCategoryDTO>builder()
                .code(200)
                .message("Ticket category updated successfully")
                .result(category)
                .timestamp(OffsetDateTime.now())
                .build());
    }

    @DeleteMapping("/{id}/ticket-categories/{categoryId}")
    @Operation(summary = "Delete a ticket category")
    public ResponseEntity<ApiResponse<Void>> deleteTicketCategory(
            @PathVariable UUID id,
            @PathVariable UUID categoryId) {
        concertService.deleteTicketCategory(id, categoryId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(200)
                .message("Ticket category deleted successfully")
                .timestamp(OffsetDateTime.now())
                .build());
    }
}
