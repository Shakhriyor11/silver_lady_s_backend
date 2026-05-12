package com.portfolio.silver_lady_s.service;


import com.portfolio.silver_lady_s.dto.product.ProductDto;

import java.util.List;

/**
 * Foydalanuvchining ko'rish tarixi asosida shaxsiy tavsiyalar.
 *
 * Algoritm:
 *   1. Foydalanuvchi eng ko'p ko'rgan kategoriyalar aniqlanadi (ProductView.viewCount).
 *   2. Shu kategoriyalardan foydalanuvchi hali ko'rmagan faol mahsulotlar tanlanadi.
 *   3. Kategoriyalarning o'rniga qarab aralashtirilib, limit soni qaytariladi.
 *
 * Agar foydalanuvchining hech qanday ko'rish tarixi bo'lmasa,
 * eng so'nggi qo'shilgan mahsulotlar qaytariladi (cold-start fallback).
 */
public interface RecommendationService {
    List<ProductDto> getRecommendations(Long userId, int limit);
}
