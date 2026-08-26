package com.portfolio.silver_lady_s.service.impl;

import com.portfolio.silver_lady_s.dto.auth.AuthResponse;
import com.portfolio.silver_lady_s.dto.auth.LoginRequest;
import com.portfolio.silver_lady_s.entity.RefreshToken;
import com.portfolio.silver_lady_s.entity.User;
import com.portfolio.silver_lady_s.entity.UserRole;
import com.portfolio.silver_lady_s.exception.UnauthorizedException;
import com.portfolio.silver_lady_s.repository.RefreshTokenRepository;
import com.portfolio.silver_lady_s.repository.UserRepository;
import com.portfolio.silver_lady_s.security.JwtService;
import com.portfolio.silver_lady_s.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${app.jwt.refresh-token-days:30}")
    private long refreshTokenDays;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest req) {
        String email = req.getEmail().trim().toLowerCase();

        User u = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!passwordEncoder.matches(req.getPassword(), u.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        if (u.getRole() != UserRole.ADMIN && u.getRole() != UserRole.CASHIER) {
            throw new UnauthorizedException("Access denied");
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

    // ─────────────────────────────────────────────────────────────────────────

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken rt = new RefreshToken();
        rt.setToken(UUID.randomUUID().toString());
        rt.setUser(user);
        rt.setExpiresAt(Instant.now().plus(refreshTokenDays, ChronoUnit.DAYS));
        refreshTokenRepository.save(rt);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rt.getToken())
                .build();
    }
}
