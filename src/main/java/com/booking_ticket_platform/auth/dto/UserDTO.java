package com.booking_ticket_platform.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Data Transfer Object cho ho so nguoi dung")
public class UserDTO {
    @Schema(description = "UUID ma dinh danh duy nhat cua nguoi dung", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Dia chi email duy nhat cua nguoi dung", example = "nguyenvana@example.com")
    private String email;

    @Schema(description = "Vai tro he thong cua nguoi dung", example = "CUSTOMER")
    private String role;
}
