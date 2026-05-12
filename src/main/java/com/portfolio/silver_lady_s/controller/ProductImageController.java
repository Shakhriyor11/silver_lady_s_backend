package com.portfolio.silver_lady_s.controller;

import com.portfolio.silver_lady_s.dto.product.ProductImageDto;
import com.portfolio.silver_lady_s.service.ProductImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Mahsulot rasmlari boshqaruvi (faqat admin).
 *
 * POST   /api/products/{id}/images               — rasm(lar) yuklash
 * DELETE /api/products/{id}/images/{imageId}     — rasmni o'chirish
 * PATCH  /api/products/{id}/images/{imageId}/primary — asosiy rasm qilish
 * PATCH  /api/products/{id}/images/reorder       — tartibni o'zgartirish
 */
@RestController
@RequestMapping("/api/products/{productId}/images")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ProductImageController {

    private final ProductImageService productImageService;

    @PostMapping(consumes = "multipart/form-data")
    public List<ProductImageDto> upload(
            @PathVariable Long productId,
            @RequestParam("files") List<MultipartFile> files
    ) {
        return productImageService.addImages(productId, files);
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long productId,
            @PathVariable Long imageId
    ) {
        productImageService.deleteImage(productId, imageId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{imageId}/primary")
    public ProductImageDto setPrimary(
            @PathVariable Long productId,
            @PathVariable Long imageId
    ) {
        return productImageService.setPrimary(productId, imageId);
    }

    @PatchMapping("/reorder")
    public List<ProductImageDto> reorder(
            @PathVariable Long productId,
            @RequestBody List<Long> orderedIds
    ) {
        return productImageService.reorder(productId, orderedIds);
    }
}
