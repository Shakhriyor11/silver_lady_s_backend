package com.portfolio.silver_lady_s.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "sale_items",
        indexes = @Index(name = "idx_sale_items_sale_id", columnList = "sale_id")
)
@Getter
@Setter
@NoArgsConstructor
public class SaleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_sale_item_sale"))
    private Sale sale;

    // Nullable — future hard-delete bo'lsa ham tarix saqlansin
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id",
            foreignKey = @ForeignKey(name = "fk_sale_item_product"))
    private Product product;

    // Sotuv vaqtidagi snapshot — mahsulot o'zgansa ham narx/nom/o'lcham saqlanadi
    @Column(nullable = false, length = 160)
    private String productName;

    @Column(name = "size", length = 30, nullable = false)
    private String size;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal lineTotal;
}
