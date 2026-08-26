package com.portfolio.silver_lady_s.dto.wishlist;

import com.portfolio.silver_lady_s.dto.product.ProductDto;
import com.portfolio.silver_lady_s.entity.WishlistItem;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class WishlistItemDto {
    private ProductDto product;
    private Instant addedAt;

    public static WishlistItemDto from(WishlistItem wi) {
        return new WishlistItemDto(ProductDto.from(wi.getProduct()), wi.getCreatedAt());
    }
}
