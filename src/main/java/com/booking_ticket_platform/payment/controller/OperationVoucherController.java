package com.booking_ticket_platform.payment.controller;

import com.booking_ticket_platform.payment.entity.Voucher;
import com.booking_ticket_platform.payment.service.VoucherService;
import com.booking_ticket_platform.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/operation/vouchers")
@Tag(name = "Operation - Voucher", description = "Endpoints for managing vouchers by Admin/Operator")
public class OperationVoucherController {

    private final VoucherService voucherService;

    public OperationVoucherController(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    @GetMapping
    @Operation(summary = "Get list of all vouchers")
    public ResponseEntity<ApiResponse<List<Voucher>>> getAllVouchers() {
        return ResponseEntity.ok(ApiResponse.<List<Voucher>>builder()
                .code(200)
                .message("Fetched vouchers successfully")
                .result(voucherService.getAllVouchers())
                .build());
    }
}
