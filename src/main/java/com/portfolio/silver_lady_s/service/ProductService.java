package com.portfolio.silver_lady_s.service;

import com.portfolio.silver_lady_s.dto.PageResponse;
import com.portfolio.silver_lady_s.dto.product.CreateProductRequest;
import com.portfolio.silver_lady_s.dto.product.ProductDto;
import com.portfolio.silver_lady_s.dto.product.UpdateProductRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    PageResponse<ProductDto> getProducts(Long categoryId, String search, Pageable pageable);
    ProductDto getById(Long id);

    ProductDto create(CreateProductRequest req);
    ProductDto update(Long id, UpdateProductRequest req);

    /** Soft delete: active=false qiladi */
    void delete(Long id);

    /** Soft deleted mahsulotni qayta faollashtiradi */
    ProductDto restore(Long id);

    /**
     * Berilgan mahsulot bilan bir xil kategoriyadan o'xshash mahsulotlar.
     * @param productId asosiy mahsulot (natijadan chiqarib tashlanadi)
     * @param limit     nechta mahsulot qaytarish
     */
    List<ProductDto> getSimilarProducts(Long productId, int limit);
}
