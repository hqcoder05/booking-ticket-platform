package com.booking_ticket_platform.auth.service;

import com.booking_ticket_platform.auth.dto.AuthResponse;
import com.booking_ticket_platform.auth.dto.ForgotPasswordRequest;
import com.booking_ticket_platform.auth.dto.LoginRequest;
import com.booking_ticket_platform.auth.dto.RegisterRequest;

public interface IAuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    void forgotPassword(ForgotPasswordRequest request);
}
