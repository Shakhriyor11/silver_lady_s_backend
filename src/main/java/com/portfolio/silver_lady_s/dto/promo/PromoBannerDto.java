package com.portfolio.silver_lady_s.dto.promo;

import com.portfolio.silver_lady_s.entity.PromoBanner;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class PromoBannerDto {
    private Long id;
    private String imageUrl;
    private String titleUz;
    private String titleRu;
    private String titleEn;
    private String subtitleUz;
    private String subtitleRu;
    private String subtitleEn;
    private Integer displayOrder;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    public static PromoBannerDto from(PromoBanner b) {
        return new PromoBannerDto(
                b.getId(), b.getImageUrl(),
                b.getTitleUz(), b.getTitleRu(), b.getTitleEn(),
                b.getSubtitleUz(), b.getSubtitleRu(), b.getSubtitleEn(),
                b.getDisplayOrder(), b.isActive(),
                b.getCreatedAt(), b.getUpdatedAt()
        );
    }
}
