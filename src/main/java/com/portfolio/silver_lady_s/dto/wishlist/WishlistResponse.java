package com.portfolio.silver_lady_s.dto.wishlist;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class WishlistResponse {
    private List<WishlistItemDto> items;
}
