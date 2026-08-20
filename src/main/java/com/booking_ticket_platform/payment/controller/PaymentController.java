package com.booking_ticket_platform.payment.controller;

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
    public ResponseEntity<ApiResponse<PaymentDTO>> initiatePayment(@RequestParam UUID bookingId, @RequestParam String method) {
        Payment payment = paymentService.initiatePayment(bookingId, method);
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

    @PostMapping("/{paymentId}/complete")
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
