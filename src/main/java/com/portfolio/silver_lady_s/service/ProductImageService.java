package com.portfolio.silver_lady_s.service;

import com.portfolio.silver_lady_s.dto.product.ProductImageDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductImageService {

    /** Bir yoki bir nechta rasm yuklaydi, yuklangan rasmlar ro'yxatini qaytaradi */
    List<ProductImageDto> addImages(Long productId, List<MultipartFile> files);

    /** Rasmni o'chiradi (diskdan ham) */
    void deleteImage(Long productId, Long imageId);

    /** Berilgan rasmni asosiy (primary) deb belgilaydi */
    ProductImageDto setPrimary(Long productId, Long imageId);

    /** Galereya tartibini yangilaydi — orderedIds ro'yxati yangi tartibni bildiradi */
    List<ProductImageDto> reorder(Long productId, List<Long> orderedIds);
}
