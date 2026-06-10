package com.portfolio.silver_lady_s.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "promo_banners",
        indexes = @Index(name = "idx_promo_banner_order", columnList = "display_order")
)
@Getter
@Setter
@NoArgsConstructor
public class PromoBanner extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 500)
    private String imageUrl;

    @Column(length = 200) private String titleUz;
    @Column(length = 200) private String titleRu;
    @Column(length = 200) private String titleEn;

    @Column(length = 400) private String subtitleUz;
    @Column(length = 400) private String subtitleRu;
    @Column(length = 400) private String subtitleEn;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(nullable = false)
    private boolean active = true;
}
