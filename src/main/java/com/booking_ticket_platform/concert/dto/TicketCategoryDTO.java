package com.booking_ticket_platform.concert.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class TicketCategoryDTO {
    private UUID id;
    private UUID concertId;
    private String name;
    private String type;
    private BigDecimal price;
    private int totalQuantity;
    private int availableQuantity;
}
