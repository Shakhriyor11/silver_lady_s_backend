package com.portfolio.silver_lady_s.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Foydalanuvchi qaysi mahsulotni ko'rganini qayd etadi.
 *
 * Har bir (user, product) juftligi uchun bitta yozuv mavjud bo'ladi.
 * Foydalanuvchi bir mahsulotni qayta ko'rsa:
 *   - viewCount oshiriladi
 *   - lastViewedAt yangilanadi
 *
 * Bu ma'lumotlar asosida RecommendationService tavsiya hosil qiladi:
 *   eng ko'p ko'rilgan kategoriyalar → shu kategoriyalardan yangi mahsulotlar.
 */
@Entity
@Table(
        name = "product_views",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_product_view_user_product",
                columnNames = {"user_id", "product_id"}
        ),
        indexes = {
                @Index(name = "idx_pv_user_id",       columnList = "user_id"),
                @Index(name = "idx_pv_last_viewed",   columnList = "last_viewed_at"),
                @Index(name = "idx_pv_view_count",    columnList = "view_count")
        }
)
@Getter @Setter @NoArgsConstructor
public class ProductView {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_pv_user"))
    private User user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_pv_product"))
    private Product product;

    /** Jami necha marta ko'rilgan */
    @Column(name = "view_count", nullable = false)
    private int viewCount = 1;

    /** Oxirgi ko'rilgan vaqt — har yangilashda o'zgaradi */
    @UpdateTimestamp
    @Column(name = "last_viewed_at", nullable = false)
    private Instant lastViewedAt;
}
