package com.booking_ticket_platform.booking.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class BookingDTO {
    private UUID id;
    private UUID userId;
    private UUID concertId;
    private String status;
    private BigDecimal totalAmount;
    private OffsetDateTime createdAt;
    private List<BookingDetailDTO> items;
}
