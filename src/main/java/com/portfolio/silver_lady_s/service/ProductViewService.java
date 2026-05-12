package com.portfolio.silver_lady_s.service;

/**
 * Foydalanuvchining mahsulot ko'rish tarixini boshqarish.
 * Har safar foydalanuvchi mahsulot sahifasini ochganda chaqiriladi.
 */
public interface ProductViewService {

    /**
     * Ko'rishni qayd etadi yoki mavjud yozuvni yangilaydi.
     * Agar (userId, productId) juftligi allaqachon mavjud bo'lsa,
     * viewCount++ va lastViewedAt yangilanadi.
     */
    void recordView(Long userId, Long productId);
}
