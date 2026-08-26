package com.portfolio.silver_lady_s.controller;

import com.portfolio.silver_lady_s.dto.wishlist.AddToWishlistRequest;
import com.portfolio.silver_lady_s.dto.wishlist.WishlistResponse;
import com.portfolio.silver_lady_s.security.CurrentUser;
import com.portfolio.silver_lady_s.service.WishlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public WishlistResponse myWishlist() {
        Long userId = CurrentUser.principal().getUserId();
        return wishlistService.getMyWishlist(userId);
    }

    @GetMapping("/ids")
    public Set<Long> myWishlistIds() {
        Long userId = CurrentUser.principal().getUserId();
        return wishlistService.getMyWishlistIds(userId);
    }

    @PostMapping("/items")
    public WishlistResponse add(@Valid @RequestBody AddToWishlistRequest req) {
        Long userId = CurrentUser.principal().getUserId();
        return wishlistService.addItem(userId, req.getProductId());
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Void> remove(@PathVariable Long productId) {
        Long userId = CurrentUser.principal().getUserId();
        wishlistService.removeItem(userId, productId);
        return ResponseEntity.noContent().build();
    }
}
