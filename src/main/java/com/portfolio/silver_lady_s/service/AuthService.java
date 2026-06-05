package com.portfolio.silver_lady_s.service;

import com.portfolio.silver_lady_s.dto.auth.AuthResponse;
import com.portfolio.silver_lady_s.dto.auth.LoginRequest;

public interface AuthService {

    AuthResponse login(LoginRequest req);

    AuthResponse refresh(String refreshToken);

    void logout(String refreshToken);

    void logoutAll(Long userId);
}
