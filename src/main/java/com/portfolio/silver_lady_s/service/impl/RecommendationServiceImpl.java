package com.portfolio.silver_lady_s.service.impl;

import com.portfolio.silver_lady_s.dto.product.ProductDto;
import com.portfolio.silver_lady_s.entity.Product;
import com.portfolio.silver_lady_s.repository.ProductRepository;
import com.portfolio.silver_lady_s.repository.ProductViewRepository;
import com.portfolio.silver_lady_s.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final ProductViewRepository productViewRepository;
    private final ProductRepository productRepository;

    /** IN (...) ro'yxati uchun maksimal ko'rilgan mahsulot soni */
    @Value("${app.recommendation.max-viewed-ids:200}")
    private int maxViewedIds;

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> getRecommendations(Long userId, int limit) {
        List<Long> topCategoryIds = productViewRepository.findTopCategoryIdsByUserId(userId);

        if (topCategoryIds.isEmpty()) {
            return productRepository
                    .findActiveExcluding(Collections.emptyList(), PageRequest.of(0, limit))
                    .stream()
                    .map(ProductDto::from)
                    .toList();
        }

        List<Long> viewedIds = productViewRepository.findViewedProductIdsByUserId(
                userId, PageRequest.of(0, maxViewedIds));

        List<Product> candidates = productRepository.findByCategoryIdsExcluding(
                topCategoryIds,
                viewedIds.isEmpty() ? Collections.singletonList(-1L) : viewedIds,
                PageRequest.of(0, limit)
        );

        if (candidates.size() < limit) {
            List<Long> fullExclude = new ArrayList<>(viewedIds);
            candidates.stream().map(Product::getId).forEach(fullExclude::add);
            if (fullExclude.isEmpty()) fullExclude.add(-1L);

            List<Product> fallback = productRepository.findActiveExcluding(
                    fullExclude,
                    PageRequest.of(0, limit - candidates.size())
            );

            candidates = new ArrayList<>(candidates);
            candidates.addAll(fallback);
        }

        return candidates.stream()
                .limit(limit)
                .map(ProductDto::from)
                .toList();
    }
}
