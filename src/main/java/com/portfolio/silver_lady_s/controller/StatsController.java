package com.portfolio.silver_lady_s.controller;

import com.portfolio.silver_lady_s.dto.stats.DailyStatsDto;
import com.portfolio.silver_lady_s.dto.stats.OrderStatusStatsDto;
import com.portfolio.silver_lady_s.dto.stats.StatsOverviewDto;
import com.portfolio.silver_lady_s.dto.stats.TopProductDto;
import com.portfolio.silver_lady_s.service.StatsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @PostMapping("/visit")
    public void recordVisit(@RequestBody Map<String, String> body,
                            HttpServletRequest request) {
        String visitorId = body.getOrDefault("visitorId", "");
        if (visitorId.isBlank()) return;
        String ipHash = sha256(getClientIp(request));
        statsService.recordVisit(visitorId, ipHash);
    }

    @GetMapping("/overview")
    @PreAuthorize("hasRole('ADMIN')")
    public StatsOverviewDto overview() {
        return statsService.getOverview();
    }

    @GetMapping("/daily")
    @PreAuthorize("hasRole('ADMIN')")
    public DailyStatsDto daily(@RequestParam(defaultValue = "30") int days) {
        int d = Math.min(Math.max(days, 7), 90);
        return statsService.getDaily(d);
    }

    @GetMapping("/order-status")
    @PreAuthorize("hasRole('ADMIN')")
    public OrderStatusStatsDto orderStatus() {
        return statsService.getOrderStatusStats();
    }

    @GetMapping("/top-products")
    @PreAuthorize("hasRole('ADMIN')")
    public List<TopProductDto> topProducts(
            @RequestParam(defaultValue = "10") int limit) {
        int l = Math.min(Math.max(limit, 1), 50);
        return statsService.getTopViewedProducts(l);
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private String getClientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        return xff != null ? xff.split(",")[0].trim() : req.getRemoteAddr();
    }

    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash).substring(0, 16);
        } catch (Exception e) {
            return "unknown";
        }
    }
}
