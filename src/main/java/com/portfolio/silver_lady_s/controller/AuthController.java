package com.portfolio.silver_lady_s.controller;

import com.portfolio.silver_lady_s.dto.auth.AuthResponse;
import com.portfolio.silver_lady_s.dto.auth.LoginRequest;
import com.portfolio.silver_lady_s.dto.auth.RefreshRequest;
import com.portfolio.silver_lady_s.dto.auth.RegisterRequest;
import com.portfolio.silver_lady_s.dto.auth.SendOtpRequest;
import com.portfolio.silver_lady_s.dto.auth.VerifyOtpRequest;
import com.portfolio.silver_lady_s.security.CurrentUser;
import com.portfolio.silver_lady_s.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest req) {
        return authService.refresh(req.getRefreshToken());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody RefreshRequest req) {
        authService.logout(req.getRefreshToken());
    }

    @PostMapping("/logout-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logoutAll() {
        authService.logoutAll(CurrentUser.principal().getUserId());
    }

    @PostMapping("/send-otp")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sendOtp(@Valid @RequestBody SendOtpRequest req) {
        authService.sendOtp(req.getPhone());
    }

    @PostMapping("/verify-otp")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verifyOtp(@Valid @RequestBody VerifyOtpRequest req) {
        authService.verifyOtp(req.getPhone(), req.getOtp());
    }
}
