package com.portfolio.silver_lady_s.config;

import com.portfolio.silver_lady_s.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Muddati o'tgan va bekor qilingan refresh tokenlarni kuniga bir marta o'chiradi.
 * Jadvalni kechasi 02:00 da ishlatish DB yukini kamaytiradi.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;

    @Scheduled(cron = "0 0 2 * * *", zone = "UTC")
    @Transactional
    public void cleanupExpiredTokens() {
        int deleted = refreshTokenRepository.deleteExpiredAndRevoked(Instant.now());
        log.info("Token cleanup: {} expired/revoked refresh token(s) removed", deleted);
    }
}