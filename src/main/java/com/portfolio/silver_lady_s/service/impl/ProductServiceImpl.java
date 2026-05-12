package com.portfolio.silver_lady_s.service.impl;

import com.portfolio.silver_lady_s.dto.PageResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    public PageResponse<ProductDto> getProducts(Long categoryId, String search, Pageable pageable) {
        String q = (search == null) ? null : search.trim();

        Page<Product> page;
        if (categoryId != null && StringUtils.hasText(q)) {
            page = productRepository
                    .findByCategoryIdAndNameContainingIgnoreCaseAndActiveTrueOrderByIdDesc(categoryId, q, pageable);
        } else if (categoryId != null) {
            page = productRepository.findByCategoryIdAndActiveTrueOrderByIdDesc(categoryId, pageable);
        } else if (StringUtils.hasText(q)) {
            page = productRepository.findByNameContainingIgnoreCaseAndActiveTrueOrderByIdDesc(q, pageable);
        } else {
            page = productRepository.findAllByActiveTrueOrderByIdDesc(pageable);
        }

        return new PageResponse<>(page.map(ProductDto::from));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto getById(Long id) {
        return ProductDto.from(productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new NotFoundException("Product not found: id=" + id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> getSimilarProducts(Long productId, int limit) {
        Product product = productRepository.findByIdAndActiveTrue(productId)
                .orElseThrow(() -> new NotFoundException("Product not found: id=" + productId));

        return productRepository
                .findSimilar(product.getCategory().getId(), productId, PageRequest.of(0, limit))
                .stream()
                .map(ProductDto::from)
                .toList();
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

        return ProductDto.from(productRepository.save(p));
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

        return ProductDto.from(productRepository.save(p));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Product p = productRepository.findWithCategoryById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: id=" + id));
        p.setActive(false);
        productRepository.save(p);
    }

    @Override
    @Transactional
    public ProductDto restore(Long id) {
        Product p = productRepository.findByIdAndActiveFalse(id)
                .orElseThrow(() -> new NotFoundException("Inactive product not found: id=" + id));
        p.setActive(true);
        return ProductDto.from(productRepository.save(p));
    }
}
