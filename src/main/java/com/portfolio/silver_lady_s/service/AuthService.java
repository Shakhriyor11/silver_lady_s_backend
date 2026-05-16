package com.portfolio.silver_lady_s.service;

import com.portfolio.silver_lady_s.dto.auth.AuthResponse;
import com.portfolio.silver_lady_s.dto.auth.LoginRequest;
import com.portfolio.silver_lady_s.dto.auth.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest req);

    AuthResponse login(LoginRequest req);

    AuthResponse refresh(String refreshToken);

    void logout(String refreshToken);

    void logoutAll(Long userId);

    void sendOtp(String phone);

    void verifyOtp(String phone, String otp);
}
