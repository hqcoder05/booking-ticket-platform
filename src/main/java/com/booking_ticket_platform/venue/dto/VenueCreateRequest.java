package com.booking_ticket_platform.venue.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VenueCreateRequest {
    @NotBlank(message = "Tên địa điểm không được để trống")
    private String name;

    private String address;

    private String city;

    @Min(value = 1, message = "Sức chứa phải lớn hơn 0")
    private int capacity;
}
