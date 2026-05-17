package com.portfolio.silver_lady_s.service.impl;

import com.portfolio.silver_lady_s.entity.Product;
import com.portfolio.silver_lady_s.entity.ProductView;
import com.portfolio.silver_lady_s.entity.User;
import com.portfolio.silver_lady_s.exception.NotFoundException;
import com.portfolio.silver_lady_s.repository.ProductRepository;
import com.portfolio.silver_lady_s.repository.ProductViewRepository;
import com.portfolio.silver_lady_s.repository.UserRepository;
import com.portfolio.silver_lady_s.service.ProductViewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductViewServiceImpl implements ProductViewService {

    private final ProductViewRepository productViewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    /**
     * Ko'rishni qayd etadi.
     *
     * Agar (userId, productId) allaqachon mavjud bo'lsa:
     *   - viewCount bir oshiriladi
     *   - lastViewedAt @UpdateTimestamp orqali avtomatik yangilanadi
     *
     * Agar yangi bo'lsa:
     *   - yangi ProductView yozuvi yaratiladi (viewCount = 1)
     *
     * Bu metod ProductController.getById() ichida non-blocking ishlaydi;
     * xato yuz bersa controller uni e'tiborsiz qoldiradi.
     */
    @Override
    @Transactional
    public void recordView(Long userId, Long productId) {
        productViewRepository
                .findByUserIdAndProductId(userId, productId)
                .ifPresentOrElse(
                        existing -> {
                            existing.setViewCount(existing.getViewCount() + 1);
                            productViewRepository.save(existing);
                        },
                        () -> {
                            User user = userRepository.findById(userId)
                                    .orElseThrow(() -> new NotFoundException("User not found: id=" + userId));
                            Product product = productRepository.findById(productId)
                                    .orElseThrow(() -> new NotFoundException("Product not found: id=" + productId));

                            ProductView pv = new ProductView();
                            pv.setUser(user);
                            pv.setProduct(product);
                            pv.setViewCount(1);
                            productViewRepository.save(pv);
                        }
                );
    }
}
