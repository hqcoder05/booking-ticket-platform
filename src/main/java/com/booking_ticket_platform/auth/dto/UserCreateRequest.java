package com.booking_ticket_platform.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Yeu cau tao ho so nguoi dung moi") 
public class UserCreateRequest {

    @NotBlank(message = "Email khong duoc de trong")
    @Email(message = "Email khong hop le")
    @Schema(description = "Dia chi email duy nhat cua nguoi dung", example = "customer@example.com")
    private String email;

    @NotBlank(message = "Mat khau khong duoc de trong")
    @Size(min = 8, message = "Mat khau phai co it nhat 8 ky tu")
    @Schema(description = "Mat khau cua nguoi dung", example = "password123")
    private String password;

    @NotNull(message = "Vai tro he thong khong duoc de trong")
    @Schema(description = "Vai tro he thong cua nguoi dung (CUSTOMER, OPERATOR, ADMIN)", example = "CUSTOMER")
    private String role;
}
