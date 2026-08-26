package com.portfolio.silver_lady_s.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "sales",
        indexes = {
                @Index(name = "idx_sales_cashier_id", columnList = "cashier_id"),
                @Index(name = "idx_sales_created_at", columnList = "created_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Sale extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cashier_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_sale_cashier"))
    private User cashier;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL,
               orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SaleItem> items = new ArrayList<>();
}
