package com.portfolio.silver_lady_s.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "order_items",
        indexes = @Index(name = "idx_order_items_order_id", columnList = "order_id")
)
@Getter
@Setter
@NoArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_order_item_order"))
    private Order order;

    // Nullable — future hard-delete bo'lsa ham tarix saqlansin
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id",
            foreignKey = @ForeignKey(name = "fk_order_item_product"))
    private Product product;

    // Buyurtma vaqtidagi snapshot — mahsulot o'zgansa ham narx/nom saqlanadi
    @Column(nullable = false, length = 160)
    private String productName;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal lineTotal;
}
