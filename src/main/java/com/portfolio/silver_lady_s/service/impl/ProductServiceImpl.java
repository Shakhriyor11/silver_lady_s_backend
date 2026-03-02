package com.portfolio.silver_lady_s.service.impl;

import com.portfolio.silver_lady_s.dto.product.CreateProductRequest;
import com.portfolio.silver_lady_s.dto.product.ProductDto;
import com.portfolio.silver_lady_s.dto.product.UpdateProductRequest;
import com.portfolio.silver_lady_s.entity.Category;
import com.portfolio.silver_lady_s.entity.Product;
import com.portfolio.silver_lady_s.exception.NotFoundException;
import com.portfolio.silver_lady_s.repository.CategoryRepository;
import com.portfolio.silver_lady_s.repository.ProductRepository;
import com.portfolio.silver_lady_s.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> getProducts(Long categoryId, String search) {
        String q = (search == null) ? null : search.trim();

        List<Product> products;
        if (categoryId != null && StringUtils.hasText(q)) {
            products = productRepository.findByCategoryIdAndNameContainingIgnoreCaseAndActiveTrueOrderByIdDesc(categoryId, q);
        } else if (categoryId != null) {
            products = productRepository.findByCategoryIdAndActiveTrueOrderByIdDesc(categoryId);
        } else if (StringUtils.hasText(q)) {
            products = productRepository.findByNameContainingIgnoreCaseAndActiveTrueOrderByIdDesc(q);
        } else {
            products = productRepository.findAllByActiveTrueOrderByIdDesc();
        }

        return products.stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto getById(Long id) {
        Product p = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new NotFoundException("Product not found: id=" + id));
        return toDto(p);
    }

    @Override
    @Transactional
    public ProductDto create(CreateProductRequest req) {
        Category c = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found: id=" + req.getCategoryId()));

        Product p = new Product();
        p.setName(req.getName().trim());
        p.setDescription(req.getDescription());
        p.setPrice(req.getPrice());
        p.setCategory(c);
        if (req.getActive() != null) p.setActive(req.getActive());

        return toDto(productRepository.save(p));
    }

    @Override
    @Transactional
    public ProductDto update(Long id, UpdateProductRequest req) {
        Product p = productRepository.findWithCategoryById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: id=" + id));

        Category c = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found: id=" + req.getCategoryId()));

        p.setName(req.getName().trim());
        p.setDescription(req.getDescription());
        p.setPrice(req.getPrice());
        p.setCategory(c);
        if (req.getActive() != null) p.setActive(req.getActive());

        return toDto(productRepository.save(p));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Product p = productRepository.findWithCategoryById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: id=" + id));
        productRepository.delete(p);
    }

    private ProductDto toDto(Product p) {
        return new ProductDto(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getPrice(),
                p.getCategory().getId(),
                p.getCategory().getName(),
                p.isActive()
        );
    }
}
