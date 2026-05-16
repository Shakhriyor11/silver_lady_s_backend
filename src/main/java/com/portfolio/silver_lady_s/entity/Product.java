package com.portfolio.silver_lady_s.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(
        name = "products",
        indexes = {
                @Index(name = "idx_product_name",       columnList = "name"),
                @Index(name = "idx_product_created_at", columnList = "created_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Product extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(length = 160) private String nameUz;
    @Column(length = 160) private String nameRu;
    @Column(length = 160) private String nameEn;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT") private String descriptionUz;
    @Column(columnDefinition = "TEXT") private String descriptionRu;
    @Column(columnDefinition = "TEXT") private String descriptionEn;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(name = "discount_percent")
    private Integer discountPercent;

    @Column(name = "discount_amount", precision = 19, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "discount_starts_at")
    private Instant discountStartsAt;

    @Column(name = "discount_ends_at")
    private Instant discountEndsAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "product_categories",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"),
            foreignKey = @ForeignKey(name = "fk_pc_product"),
            inverseForeignKey = @ForeignKey(name = "fk_pc_category")
    )
    private Set<Category> categories = new HashSet<>();

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL,
               orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC, id ASC")
    private List<ProductImage> images = new ArrayList<>();
}
