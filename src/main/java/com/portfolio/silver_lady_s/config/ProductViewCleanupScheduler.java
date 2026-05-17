package com.portfolio.silver_lady_s.config;

import com.portfolio.silver_lady_s.repository.ProductViewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Eski ProductView yozuvlarini har oyda bir marta tozalaydi.
 * N oydan ko'proq ko'rilmagan yozuvlar o'chiriladi (default: 6 oy).
 * Bu recommendation query samaradorligini saqlab, jadvalni cheksiz o'sishdan to'xtatadi.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductViewCleanupScheduler {

    private final ProductViewRepository productViewRepository;

    @Value("${app.product-view.retention-days:180}")
    private int retentionDays;

    @Scheduled(cron = "0 0 3 1 * *", zone = "UTC")
    @Transactional
    public void cleanupOldViews() {
        Instant cutoff = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
        int deleted = productViewRepository.deleteOlderThan(cutoff);
        log.info("ProductView cleanup: {} record(s) older than {} days removed", deleted, retentionDays);
    }
}
