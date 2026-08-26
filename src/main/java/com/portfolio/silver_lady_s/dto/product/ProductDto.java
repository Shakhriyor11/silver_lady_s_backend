package com.portfolio.silver_lady_s.dto.product;

import com.portfolio.silver_lady_s.entity.Category;
import com.portfolio.silver_lady_s.entity.Product;
import com.portfolio.silver_lady_s.util.PriceCalculator;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Getter
@AllArgsConstructor
public class ProductDto {

    private Long id;
    private String name;
    private String nameUz;
    private String nameRu;
    private String nameEn;
    private String description;
    private String descriptionUz;
    private String descriptionRu;
    private String descriptionEn;
    private int stockQuantity;
    private String barcode;
    private BigDecimal price;
    private BigDecimal salePrice;
    private Integer discountPercent;
    private BigDecimal discountAmount;
    private Instant discountStartsAt;
    private Instant discountEndsAt;
    private List<CategoryInfo> categories;
    private List<SizeEntryDto> sizeEntries;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
    private List<ProductImageDto> images;

    public static ProductDto from(Product p) {
        List<CategoryInfo> cats = p.getCategories().stream()
                .sorted(Comparator.comparing(Category::getId))
                .map(c -> new CategoryInfo(c.getId(), c.getName(), c.getNameUz(), c.getNameRu(), c.getNameEn()))
                .toList();

        List<ProductImageDto> imgs = p.getImages().stream()
                .map(ProductImageDto::from)
                .toList();

        return new ProductDto(
                p.getId(),
                p.getName(),
                p.getNameUz(),
                p.getNameRu(),
                p.getNameEn(),
                p.getDescription(),
                p.getDescriptionUz(),
                p.getDescriptionRu(),
                p.getDescriptionEn(),
                p.getStockQuantity(),
                p.getBarcode(),
                p.getPrice(),
                PriceCalculator.computeSalePrice(p),
                p.getDiscountPercent(),
                p.getDiscountAmount(),
                p.getDiscountStartsAt(),
                p.getDiscountEndsAt(),
                cats,
                p.getSizeEntries().stream().map(SizeEntryDto::from).toList(),
                p.isActive(),
                p.getCreatedAt(),
                p.getUpdatedAt(),
                imgs
        );
    }

    @Getter
    @AllArgsConstructor
    public static class CategoryInfo {
        private Long id;
        private String name;
        private String nameUz;
        private String nameRu;
        private String nameEn;
    }
}
