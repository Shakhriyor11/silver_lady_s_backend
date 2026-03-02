package com.portfolio.silver_lady_s.controller;

import com.portfolio.silver_lady_s.dto.cart.AddToCartRequest;
import com.portfolio.silver_lady_s.dto.cart.CartResponse;
import com.portfolio.silver_lady_s.dto.cart.UpdateCartItemRequest;
import com.portfolio.silver_lady_s.security.CurrentUser;
import com.portfolio.silver_lady_s.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public CartResponse myCart() {
        Long userId = CurrentUser.principal().getUserId();
        return cartService.getMyCart(userId);
    }

    @PostMapping("/items")
    public CartResponse add(@Valid @RequestBody AddToCartRequest req) {
        Long userId = CurrentUser.principal().getUserId();
        return cartService.addItem(userId, req);
    }

    @PutMapping("/items")
    public CartResponse update(@Valid @RequestBody UpdateCartItemRequest req) {
        Long userId = CurrentUser.principal().getUserId();
        return cartService.updateItem(userId, req);
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Void> remove(@PathVariable Long productId) {
        Long userId = CurrentUser.principal().getUserId();
        cartService.removeItem(userId, productId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clear() {
        Long userId = CurrentUser.principal().getUserId();
        cartService.clear(userId);
        return ResponseEntity.noContent().build();
    }
}
