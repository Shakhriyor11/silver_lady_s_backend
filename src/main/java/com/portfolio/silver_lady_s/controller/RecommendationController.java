package com.portfolio.silver_lady_s.controller;

import com.portfolio.silver_lady_s.dto.product.ProductDto;
import com.portfolio.silver_lady_s.security.CurrentUser;
import com.portfolio.silver_lady_s.service.RecommendationService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Foydalanuvchining ko'rgan mahsulotlari asosida shaxsiy tavsiyalar.
 * GET /api/recommendations  — autentifikatsiyadan o'tgan foydalanuvchilar uchun.
 */
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@Validated
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping
    public List<ProductDto> getRecommendations(
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit
    ) {
        Long userId = CurrentUser.principal().getUserId();
        return recommendationService.getRecommendations(userId, limit);
    }
}
