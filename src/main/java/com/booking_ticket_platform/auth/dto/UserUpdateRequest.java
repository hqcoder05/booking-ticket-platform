package com.booking_ticket_platform.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Yeu cau cap nhat ho so nguoi dung")
public class UserUpdateRequest {

    @NotBlank(message = "Email khong duoc de trong")
    @Email(message = "Email khong hop le")
    @Schema(description = "Dia chi email duy nhat cua nguoi dung", example = "customer@example.com")
    private String email;

    @Size(min = 8, message = "Mat khau phai co it nhat 8 ky tu")
    @Schema(description = "Mat khau moi tuy chon. De trong neu khong muon doi mat khau", example = "newPassword123")
    private String password;
}
