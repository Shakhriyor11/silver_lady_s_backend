package com.portfolio.silver_lady_s.service;

import com.portfolio.silver_lady_s.dto.product.CreateProductRequest;
import com.portfolio.silver_lady_s.dto.product.ProductDto;
import com.portfolio.silver_lady_s.dto.product.UpdateProductRequest;

import java.util.List;

public interface ProductService {
    List<ProductDto> getProducts(Long categoryId, String search);
    ProductDto getById(Long id);

    ProductDto create(CreateProductRequest req);
    ProductDto update(Long id, UpdateProductRequest req);
    void delete(Long id);
}
