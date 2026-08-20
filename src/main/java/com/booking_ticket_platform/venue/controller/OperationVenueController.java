package com.booking_ticket_platform.venue.controller;

import com.booking_ticket_platform.shared.dto.ApiResponse;
import com.booking_ticket_platform.venue.dto.VenueCreateRequest;
import com.booking_ticket_platform.venue.dto.VenueDTO;
import com.booking_ticket_platform.venue.service.IVenueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/operation/venues")
@Tag(name = "Operation - Venue", description = "Endpoints for managing venues by Admin/Operator")
public class OperationVenueController {

    private final IVenueService venueService;

    public OperationVenueController(IVenueService venueService) {
        this.venueService = venueService;
    }

    @PostMapping
    @Operation(summary = "Create a new venue")
    public ResponseEntity<ApiResponse<VenueDTO>> createVenue(@Valid @RequestBody VenueCreateRequest request) {
        VenueDTO venue = venueService.createVenue(request);
        return ResponseEntity.ok(ApiResponse.<VenueDTO>builder()
                .code(200)
                .message("Venue created successfully")
                .result(venue)
                .timestamp(OffsetDateTime.now())
                .build());
    }

    @GetMapping
    @Operation(summary = "Get list of all venues")
    public ResponseEntity<ApiResponse<List<VenueDTO>>> getAllVenues() {
        List<VenueDTO> venues = venueService.getAllVenues();
        return ResponseEntity.ok(ApiResponse.<List<VenueDTO>>builder()
                .code(200)
                .message("Fetched venues successfully")
                .result(venues)
                .timestamp(OffsetDateTime.now())
                .build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a venue")
    public ResponseEntity<ApiResponse<Void>> deleteVenue(@PathVariable java.util.UUID id) {
        venueService.deleteVenue(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(200)
                .message("Venue deleted successfully")
                .timestamp(OffsetDateTime.now())
                .build());
    }
}
