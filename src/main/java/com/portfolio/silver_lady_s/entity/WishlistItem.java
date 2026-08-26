package com.portfolio.silver_lady_s.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "wishlist_items",
        uniqueConstraints = @UniqueConstraint(name = "uk_wishlist_user_product", columnNames = {"user_id", "product_id"}),
        indexes = @Index(name = "idx_wishlist_user_id", columnList = "user_id")
)
@Getter
@Setter
@NoArgsConstructor
public class WishlistItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_wishlist_user"))
    private User user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_wishlist_product"))
    private Product product;
}
