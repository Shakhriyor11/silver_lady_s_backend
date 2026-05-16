package com.portfolio.silver_lady_s.service;

import com.portfolio.silver_lady_s.dto.PageResponse;
import com.portfolio.silver_lady_s.dto.product.CreateProductRequest;
import com.portfolio.silver_lady_s.dto.product.ProductDto;
import com.portfolio.silver_lady_s.dto.product.UpdateProductRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {

    PageResponse<ProductDto> getProducts(Long categoryId, String search, Pageable pageable);

    PageResponse<ProductDto> getArchivedProducts(Pageable pageable);

    ProductDto getById(Long id);

    List<ProductDto> getSimilarProducts(Long productId, int limit);

    ProductDto create(CreateProductRequest req);

    ProductDto update(Long id, UpdateProductRequest req);

    void delete(Long id);

    ProductDto restore(Long id);
}
