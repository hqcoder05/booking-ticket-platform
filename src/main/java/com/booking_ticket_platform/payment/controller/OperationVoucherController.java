package com.booking_ticket_platform.payment.controller;

import com.booking_ticket_platform.payment.entity.Voucher;
import com.booking_ticket_platform.payment.service.VoucherService;
import com.booking_ticket_platform.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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

    @PostMapping
    @Operation(summary = "Create a new voucher")
    public ResponseEntity<ApiResponse<Voucher>> createVoucher(@Valid @RequestBody com.booking_ticket_platform.payment.dto.VoucherCreateRequest request) {
        Voucher created = voucherService.createVoucher(request);
        return ResponseEntity.ok(ApiResponse.<Voucher>builder()
                .code(201)
                .message("Voucher created successfully")
                .result(created)
                .build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get voucher by ID")
    public ResponseEntity<ApiResponse<Voucher>> getVoucherById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.<Voucher>builder()
                .code(200)
                .message("Fetched voucher successfully")
                .result(voucherService.getVoucherById(id))
                .build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a voucher (only if unused)")
    public ResponseEntity<ApiResponse<Void>> deleteVoucher(@PathVariable UUID id) {
        voucherService.deleteVoucher(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(200)
                .message("Voucher deleted successfully")
                .build());
    }
}
