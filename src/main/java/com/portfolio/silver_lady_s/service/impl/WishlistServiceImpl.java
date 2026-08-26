package com.portfolio.silver_lady_s.service.impl;

import com.portfolio.silver_lady_s.dto.wishlist.WishlistItemDto;
import com.portfolio.silver_lady_s.dto.wishlist.WishlistResponse;
import com.portfolio.silver_lady_s.entity.Product;
import com.portfolio.silver_lady_s.entity.User;
import com.portfolio.silver_lady_s.entity.WishlistItem;
import com.portfolio.silver_lady_s.exception.NotFoundException;
import com.portfolio.silver_lady_s.repository.ProductRepository;
import com.portfolio.silver_lady_s.repository.UserRepository;
import com.portfolio.silver_lady_s.repository.WishlistItemRepository;
import com.portfolio.silver_lady_s.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistItemRepository wishlistItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public WishlistResponse getMyWishlist(Long userId) {
        return toResponse(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> getMyWishlistIds(Long userId) {
        return wishlistItemRepository.findProductIdsByUserId(userId);
    }

    @Override
    @Transactional
    public WishlistResponse addItem(Long userId, Long productId) {
        if (!wishlistItemRepository.existsByUserIdAndProductId(userId, productId)) {
            Product product = productRepository.findByIdAndActiveTrue(productId)
                    .orElseThrow(() -> new NotFoundException("Product not found: id=" + productId));
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("User not found: id=" + userId));

            WishlistItem item = new WishlistItem();
            item.setUser(user);
            item.setProduct(product);
            wishlistItemRepository.save(item);
        }

        return toResponse(userId);
    }

    @Override
    @Transactional
    public void removeItem(Long userId, Long productId) {
        wishlistItemRepository.deleteByUserIdAndProductId(userId, productId);
    }

    private WishlistResponse toResponse(Long userId) {
        return new WishlistResponse(
                wishlistItemRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                        .map(WishlistItemDto::from)
                        .toList()
        );
    }
}
