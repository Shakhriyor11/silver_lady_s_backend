package com.portfolio.silver_lady_s.service.impl;

import com.portfolio.silver_lady_s.dto.auth.AuthResponse;
import com.portfolio.silver_lady_s.dto.auth.LoginRequest;
import com.portfolio.silver_lady_s.dto.auth.RegisterRequest;
import com.portfolio.silver_lady_s.entity.RefreshToken;
import com.portfolio.silver_lady_s.entity.User;
import com.portfolio.silver_lady_s.entity.UserRole;
import com.portfolio.silver_lady_s.exception.BadRequestException;
import com.portfolio.silver_lady_s.exception.ConflictException;
import com.portfolio.silver_lady_s.exception.NotFoundException;
import com.portfolio.silver_lady_s.exception.UnauthorizedException;
import com.portfolio.silver_lady_s.repository.RefreshTokenRepository;
import com.portfolio.silver_lady_s.repository.UserRepository;
import com.portfolio.silver_lady_s.security.JwtService;
import com.portfolio.silver_lady_s.service.AuthService;
import com.portfolio.silver_lady_s.service.SmsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SmsService smsService;

    @Value("${app.jwt.refresh-token-days:30}")
    private long refreshTokenDays;

    @Value("${app.otp.ttl-minutes:5}")
    private long otpTtlMinutes;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest req) {
        String email = req.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Email already registered");
        }

        User u = new User();
        u.setFullName(req.getFullName().trim());
        u.setEmail(email);
        u.setPhone(req.getPhone() == null ? null : normalizePhone(req.getPhone().trim()));
        u.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        u.setRole(UserRole.USER);

        User saved = userRepository.save(u);

        if (saved.getPhone() != null) {
            trySendOtp(saved);
        }

        return buildAuthResponse(saved);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest req) {
        String email = req.getEmail().trim().toLowerCase();

        User u = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!passwordEncoder.matches(req.getPassword(), u.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        return buildAuthResponse(u);
    }

    @Override
    @Transactional
    public AuthResponse refresh(String refreshTokenStr) {
        RefreshToken rt = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (rt.isRevoked()) {
            refreshTokenRepository.revokeAllByUserId(rt.getUser().getId());
            throw new UnauthorizedException("Refresh token already used — all sessions revoked");
        }

        if (rt.getExpiresAt().isBefore(Instant.now())) {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
            throw new UnauthorizedException("Refresh token expired");
        }

        rt.setRevoked(true);
        refreshTokenRepository.save(rt);

        return buildAuthResponse(rt.getUser());
    }

    @Override
    @Transactional
    public void logout(String refreshTokenStr) {
        refreshTokenRepository.findByToken(refreshTokenStr).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }

    @Override
    @Transactional
    public void logoutAll(Long userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    @Override
    @Transactional
    public void sendOtp(String phone) {
        String normalized = normalizePhone(phone);
        User user = userRepository.findByPhone(normalized)
                .orElseThrow(() -> new NotFoundException("No user found with phone: " + phone));
        trySendOtp(user);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void verifyOtp(String phone, String otp) {
        String normalized = normalizePhone(phone);
        User user = userRepository.findByPhone(normalized)
                .orElseThrow(() -> new NotFoundException("No user found with phone: " + phone));

        if (user.getOtp() == null || !user.getOtp().equals(otp.trim())) {
            throw new BadRequestException("Invalid OTP code");
        }
        if (user.getOtpExpiresAt() == null || Instant.now().isAfter(user.getOtpExpiresAt())) {
            throw new BadRequestException("OTP expired. Request a new one.");
        }

        user.setPhoneVerified(true);
        user.setOtp(null);
        user.setOtpExpiresAt(null);
        userRepository.save(user);
    }

    // ─────────────────────────────────────────────────────────────────────────

    private void trySendOtp(User user) {
        String code = generateOtp();
        user.setOtp(code);
        user.setOtpExpiresAt(Instant.now().plus(otpTtlMinutes, ChronoUnit.MINUTES));
        try {
            smsService.send(user.getPhone(),
                    "Silver Lady's - tasdiqlash kodi: " + code + ". " + otpTtlMinutes + " daqiqa ichida kiriting.");
        } catch (Exception e) {
            log.warn("Could not send OTP SMS to {}: {}", user.getPhone(), e.getMessage());
        }
    }

    private String generateOtp() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    private String normalizePhone(String phone) {
        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.startsWith("998") && digits.length() == 12) return digits;
        if (digits.length() == 9) return "998" + digits;
        if (digits.startsWith("0") && digits.length() == 10) return "998" + digits.substring(1);
        return digits;
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken rt = new RefreshToken();
        rt.setToken(UUID.randomUUID().toString());
        rt.setUser(user);
        rt.setExpiresAt(Instant.now().plus(refreshTokenDays, ChronoUnit.DAYS));
        refreshTokenRepository.save(rt);
        return new AuthResponse(accessToken, rt.getToken());
    }
}
