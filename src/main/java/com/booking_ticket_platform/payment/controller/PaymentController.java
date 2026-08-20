package com.booking_ticket_platform.payment.controller;

import com.booking_ticket_platform.payment.dto.PaymentInitiateRequest;
import com.booking_ticket_platform.payment.service.PaymentService;
import com.booking_ticket_platform.payment.entity.Payment;
import com.booking_ticket_platform.payment.dto.PaymentDTO;
import com.booking_ticket_platform.shared.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/customer/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/initiate")
    public ResponseEntity<ApiResponse<PaymentDTO>> initiatePayment(@RequestBody PaymentInitiateRequest request) {
        Payment payment = paymentService.initiatePayment(request.getBookingId(), request.getMethod());
        PaymentDTO dto = PaymentDTO.builder()
                .id(payment.getId())
                .bookingId(payment.getBooking().getId())
                .status(payment.getStatus())
                .method(payment.getMethod())
                .build();
        return ResponseEntity.ok(ApiResponse.<PaymentDTO>builder()
                .code(200)
                .message("Payment initiated")
                .result(dto)
                .build());
    }

    @PutMapping("/{paymentId}/complete")
    public ResponseEntity<ApiResponse<PaymentDTO>> completePayment(@PathVariable UUID paymentId) {
        Payment payment = paymentService.completePayment(paymentId);
        PaymentDTO dto = PaymentDTO.builder()
                .id(payment.getId())
                .bookingId(payment.getBooking().getId())
                .status(payment.getStatus())
                .method(payment.getMethod())
                .build();
        return ResponseEntity.ok(ApiResponse.<PaymentDTO>builder()
                .code(200)
                .message("Payment completed successfully")
                .result(dto)
                .build());
    }
}
