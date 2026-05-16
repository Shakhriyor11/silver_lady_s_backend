package com.portfolio.silver_lady_s.dto.product;

import com.portfolio.silver_lady_s.entity.Category;
import com.portfolio.silver_lady_s.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private BigDecimal price;
    private BigDecimal salePrice;
    private Integer discountPercent;
    private BigDecimal discountAmount;
    private Instant discountStartsAt;
    private Instant discountEndsAt;
    private List<CategoryInfo> categories;
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
                p.getPrice(),
                computeSalePrice(p),
                p.getDiscountPercent(),
                p.getDiscountAmount(),
                p.getDiscountStartsAt(),
                p.getDiscountEndsAt(),
                cats,
                p.isActive(),
                p.getCreatedAt(),
                p.getUpdatedAt(),
                imgs
        );
    }

    private static BigDecimal computeSalePrice(Product p) {
        BigDecimal price = p.getPrice();
        Instant now = Instant.now();

        boolean inWindow = (p.getDiscountStartsAt() == null || !now.isBefore(p.getDiscountStartsAt()))
                        && (p.getDiscountEndsAt()   == null || now.isBefore(p.getDiscountEndsAt()));

        if (!inWindow) return price;

        if (p.getDiscountPercent() != null && p.getDiscountPercent() > 0) {
            BigDecimal factor = BigDecimal.ONE.subtract(
                    BigDecimal.valueOf(p.getDiscountPercent()).divide(BigDecimal.valueOf(100)));
            return price.multiply(factor).setScale(2, RoundingMode.HALF_UP);
        }
        if (p.getDiscountAmount() != null && p.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal result = price.subtract(p.getDiscountAmount());
            return result.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : result.setScale(2, RoundingMode.HALF_UP);
        }
        return price;
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
