package com.booking_ticket_platform.concert.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class TicketCategoryCreateRequest {
    @NotBlank(message = "Category name is required")
    private String name;

    @NotBlank(message = "Category type (e.g. SEATED, STANDING) is required")
    private String type;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price must be greater than or equal to 0")
    private BigDecimal price;

    @Min(value = 1, message = "Total quantity must be at least 1")
    private int totalQuantity;
}
