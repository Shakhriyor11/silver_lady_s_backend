package com.portfolio.silver_lady_s.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "product_images",
        indexes = {
                @Index(name = "idx_pi_product_id",      columnList = "product_id"),
                @Index(name = "idx_pi_product_primary", columnList = "product_id, is_primary")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class ProductImage extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_image_product"))
    private Product product;

    /** Fayl servis qilinadigan URL: /uploads/products/{productId}/{uuid}.jpg */
    @Column(nullable = false, length = 500)
    private String url;

    @Column(length = 255)
    private String originalFilename;

    /** Bayt hisobida */
    @Column
    private Long fileSize;

    @Column(length = 50)
    private String contentType;

    /** Galereya tartibini belgilaydi — kichik raqam oldin ko'rsatiladi */
    @Column(name = "display_order", nullable = false)
    private int displayOrder = 0;

    /** Asosiy rasm — listing va preview uchun ishlatiladi */
    @Column(name = "is_primary", nullable = false)
    private boolean primary = false;
}
