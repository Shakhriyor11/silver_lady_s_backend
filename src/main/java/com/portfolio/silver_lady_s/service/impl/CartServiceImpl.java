package com.portfolio.silver_lady_s.service.impl;

import com.portfolio.silver_lady_s.dto.cart.*;
import com.portfolio.silver_lady_s.entity.*;
import com.portfolio.silver_lady_s.exception.NotFoundException;
import com.portfolio.silver_lady_s.repository.CartItemRepository;
import com.portfolio.silver_lady_s.repository.CartRepository;
import com.portfolio.silver_lady_s.repository.ProductRepository;
import com.portfolio.silver_lady_s.repository.UserRepository;
import com.portfolio.silver_lady_s.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public CartResponse getMyCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return toResponse(cart.getId());
    }

    @Override
    @Transactional
    public CartResponse addItem(Long userId, AddToCartRequest req) {
        Cart cart = getOrCreateCart(userId);

        Product product = productRepository.findWithCategoryById(req.getProductId())
                .orElseThrow(() -> new NotFoundException("Product not found: id=" + req.getProductId()));

        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
                .orElseGet(() -> {
                    CartItem ci = new CartItem();
                    ci.setCart(cart);
                    ci.setProduct(product);
                    ci.setQuantity(0);
                    ci.setUnitPrice(product.getPrice());
                    return ci;
                });

        item.setQuantity(item.getQuantity() + req.getQuantity());
        item.setUnitPrice(product.getPrice());
        cartItemRepository.save(item);

        return toResponse(cart.getId());
    }

    @Override
    @Transactional
    public CartResponse updateItem(Long userId, UpdateCartItemRequest req) {
        Cart cart = getOrCreateCart(userId);

        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), req.getProductId())
                .orElseThrow(() -> new NotFoundException("Cart item not found: productId=" + req.getProductId()));

        item.setQuantity(req.getQuantity());
        cartItemRepository.save(item);

        return toResponse(cart.getId());
    }

    @Override
    @Transactional
    public void removeItem(Long userId, Long productId) {
        Cart cart = getOrCreateCart(userId);
        cartItemRepository.deleteByCartIdAndProductId(cart.getId(), productId);
    }

    @Override
    @Transactional
    public void clear(Long userId) {
        Cart cart = getOrCreateCart(userId);
        cartItemRepository.deleteByCartId(cart.getId());
    }

    // Race condition fix: parallel request ikkalasi ham cart yaratmoqchi bo'lsa,
    // DataIntegrityViolationException tutib, bazada mavjud cart qaytariladi.
    @Transactional
    protected Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("User not found: id=" + userId));
            try {
                Cart cart = new Cart();
                cart.setUser(user);
                return cartRepository.saveAndFlush(cart);
            } catch (DataIntegrityViolationException ex) {
                // Parallel request allaqachon cart yaratib qo'ygan — mavjudini qaytaramiz
                return cartRepository.findByUserId(userId)
                        .orElseThrow(() -> new NotFoundException("User not found: id=" + userId));
            }
        });
    }

    private CartResponse toResponse(Long cartId) {
        List<CartItemResponse> items = cartItemRepository.findByCartId(cartId).stream().map(ci -> {
            BigDecimal lineTotal = ci.getUnitPrice().multiply(BigDecimal.valueOf(ci.getQuantity()));
            return new CartItemResponse(
                    ci.getProduct().getId(),
                    ci.getProduct().getName(),
                    ci.getQuantity(),
                    ci.getUnitPrice(),
                    lineTotal,
                    ci.getProduct().getCategory().getId(),
                    ci.getProduct().getCategory().getName()
            );
        }).toList();

        BigDecimal total = items.stream()
                .map(CartItemResponse::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(items, total);
    }
}
