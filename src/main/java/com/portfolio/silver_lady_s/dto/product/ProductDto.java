package com.portfolio.silver_lady_s.dto.product;

import com.portfolio.silver_lady_s.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@AllArgsConstructor
public class ProductDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Long categoryId;
    private String categoryName;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
    private List<ProductImageDto> images;

    public static ProductDto from(Product p) {
        List<ProductImageDto> images = p.getImages().stream()
                .map(ProductImageDto::from)
                .toList();

        return new ProductDto(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getPrice(),
                p.getCategory().getId(),
                p.getCategory().getName(),
                p.isActive(),
                p.getCreatedAt(),
                p.getUpdatedAt(),
                images
        );
    }
}
