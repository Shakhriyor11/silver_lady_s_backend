package com.portfolio.silver_lady_s.service;

import com.portfolio.silver_lady_s.dto.wishlist.WishlistResponse;

import java.util.Set;

public interface WishlistService {
    WishlistResponse getMyWishlist(Long userId);
    Set<Long> getMyWishlistIds(Long userId);
    WishlistResponse addItem(Long userId, Long productId);
    void removeItem(Long userId, Long productId);
}
