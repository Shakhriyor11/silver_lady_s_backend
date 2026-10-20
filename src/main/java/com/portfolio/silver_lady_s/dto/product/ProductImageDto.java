package com.portfolio.silver_lady_s.dto.product;

import com.portfolio.silver_lady_s.entity.ProductImage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductImageDto {
    private Long id;
    private String url;
    private boolean primary;
    private int displayOrder;
    private String altText;

    public static ProductImageDto from(ProductImage img) {
        return new ProductImageDto(
                img.getId(),
                img.getUrl(),
                img.isPrimary(),
                img.getDisplayOrder(),
                img.getAltText()
        );
    }
}
