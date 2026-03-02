package com.portfolio.silver_lady_s.service;

import com.portfolio.silver_lady_s.dto.cart.AddToCartRequest;
import com.portfolio.silver_lady_s.dto.cart.CartResponse;
import com.portfolio.silver_lady_s.dto.cart.UpdateCartItemRequest;

public interface CartService {
    CartResponse getMyCart(Long userId);
    CartResponse addItem(Long userId, AddToCartRequest req);
    CartResponse updateItem(Long userId, UpdateCartItemRequest req);
    void removeItem(Long userId, Long productId);
    void clear(Long userId);
}
