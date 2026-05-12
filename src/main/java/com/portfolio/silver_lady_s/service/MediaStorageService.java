package com.portfolio.silver_lady_s.service;

import org.springframework.web.multipart.MultipartFile;

public interface MediaStorageService {

    /**
     * Faylni saqlaydi va uning public URL ni qaytaradi.
     * URL format: /uploads/products/{productId}/{uuid}.{ext}
     */
    String store(MultipartFile file, Long productId);

    /**
     * URL bo'yicha faylni diskdan o'chiradi.
     */
    void delete(String url);
}
