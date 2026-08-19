package com.booking_ticket_platform.auth.controller;

import com.booking_ticket_platform.auth.dto.AuthResponse;
import com.booking_ticket_platform.auth.dto.ForgotPasswordRequest;
import com.booking_ticket_platform.auth.dto.LoginRequest;
import com.booking_ticket_platform.auth.dto.RegisterRequest;
import com.booking_ticket_platform.auth.service.IAuthService;
import com.booking_ticket_platform.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for login, register, and forgot password")
public class AuthController {

    private final IAuthService authService;

    public AuthController(IAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new customer")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse authResponse = authService.register(request);
        return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                .code(200)
                .message("Register successfully")
                .result(authResponse)
                .timestamp(OffsetDateTime.now())
                .build());
    }

    @PostMapping("/login")
    @Operation(summary = "Login to get access token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                .code(200)
                .message("Login successfully")
                .result(authResponse)
                .timestamp(OffsetDateTime.now())
                .build());
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request a password reset link")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(200)
                .message("If the email is valid, a reset link will be sent.")
                .timestamp(OffsetDateTime.now())
                .build());
    }
}
