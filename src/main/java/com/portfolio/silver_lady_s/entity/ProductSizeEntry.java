package com.portfolio.silver_lady_s.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "product_size_stock",
        uniqueConstraints = @UniqueConstraint(name = "uk_product_size", columnNames = {"product_id", "size"})
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductSizeEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "size", length = 30, nullable = false)
    private String size;

    @Column(name = "quantity", nullable = false)
    private int quantity = 0;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;
}
