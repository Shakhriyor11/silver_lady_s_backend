package com.portfolio.silver_lady_s.service;

import com.portfolio.silver_lady_s.dto.PageResponse;
import com.portfolio.silver_lady_s.dto.product.CreateProductRequest;
import com.portfolio.silver_lady_s.dto.product.ProductDto;
import com.portfolio.silver_lady_s.dto.product.SizeCountDto;
import com.portfolio.silver_lady_s.dto.product.UpdateProductRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface ProductService {

    PageResponse<ProductDto> getProducts(Long categoryId, String search, String sort, String sizeFilter,
                                          boolean includeArchived, Pageable pageable);

    List<SizeCountDto> getAvailableSizes(Long categoryId);

    PageResponse<ProductDto> getArchivedProducts(Pageable pageable);

    ProductDto getById(Long id);

    ProductDto getByIdAny(Long id);

    List<ProductDto> getSimilarProducts(Long productId, int limit);

    ProductDto create(CreateProductRequest req);

    ProductDto update(Long id, UpdateProductRequest req);

    void archive(Long id);

    void permanentDelete(Long id);

    ProductDto restore(Long id);

    int applyCategoryDiscount(Long categoryId, Integer discountPercent, BigDecimal discountAmount,
                               Instant discountStartsAt, Instant discountEndsAt);
}
