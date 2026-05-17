package com.portfolio.silver_lady_s.dto.carousel;

import com.portfolio.silver_lady_s.entity.CarouselItem;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class CarouselItemDto {
    private Long id;
    private String imageUrl;
    private String titleUz;
    private String titleRu;
    private String titleEn;
    private String subtitleUz;
    private String subtitleRu;
    private String subtitleEn;
    private String link;
    private Integer displayOrder;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    public static CarouselItemDto from(CarouselItem c) {
        return new CarouselItemDto(
                c.getId(), c.getImageUrl(),
                c.getTitleUz(), c.getTitleRu(), c.getTitleEn(),
                c.getSubtitleUz(), c.getSubtitleRu(), c.getSubtitleEn(),
                c.getLink(), c.getDisplayOrder(), c.isActive(),
                c.getCreatedAt(), c.getUpdatedAt()
        );
    }
}
