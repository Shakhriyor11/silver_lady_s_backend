package com.portfolio.silver_lady_s.service;

import org.springframework.web.multipart.MultipartFile;

public interface MediaStorageService {

    /**
     * Mahsulot rasmi uchun: /uploads/products/{productId}/{uuid}.{ext}
     */
    String store(MultipartFile file, Long productId);

    /**
     * Umumiy papka uchun: /uploads/{folder}/{uuid}.{ext}
     */
    String storeInFolder(MultipartFile file, String folder);

    /**
     * URL bo'yicha faylni diskdan o'chiradi.
     */
    void delete(String url);
}
