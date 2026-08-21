package com.booking_ticket_platform.payment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class VoucherCreateRequest {

    @NotBlank(message = "Mã voucher không được để trống")
    private String code;

    @NotBlank(message = "Loại giảm giá không được để trống")
    private String discountType;

    @NotNull(message = "Giá trị giảm giá không được để trống")
    @Min(value = 1, message = "Giá trị giảm giá phải lớn hơn 0")
    private BigDecimal discountValue;

    @Min(value = 1, message = "Số lượt sử dụng tối đa phải lớn hơn 0")
    private int maxUsage;
}
