package com.portfolio.silver_lady_s.service.impl;

import com.portfolio.silver_lady_s.config.CacheConfig;
import com.portfolio.silver_lady_s.dto.category.CategoryDto;
import com.portfolio.silver_lady_s.dto.category.CreateCategoryRequest;
import com.portfolio.silver_lady_s.dto.category.UpdateCategoryRequest;
import com.portfolio.silver_lady_s.entity.Category;
import com.portfolio.silver_lady_s.exception.ConflictException;
import com.portfolio.silver_lady_s.exception.NotFoundException;
import com.portfolio.silver_lady_s.repository.CategoryRepository;
import com.portfolio.silver_lady_s.repository.ProductRepository;
import com.portfolio.silver_lady_s.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(CacheConfig.CACHE_CATEGORIES)
    public List<CategoryDto> getAll() {
        return categoryRepository.findAllByOrderByIdAsc().stream()
                .map(CategoryDto::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getById(Long id) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found: id=" + id));
        return CategoryDto.from(c);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_CATEGORIES, allEntries = true)
    public CategoryDto create(CreateCategoryRequest request) {
        String name = request.getName().trim();
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Category already exists: " + name);
        }
        Category c = new Category();
        c.setName(name);
        c.setNameUz(request.getNameUz());
        c.setNameRu(request.getNameRu());
        c.setNameEn(request.getNameEn());
        return CategoryDto.from(categoryRepository.save(c));
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_CATEGORIES, allEntries = true)
    public CategoryDto update(Long id, UpdateCategoryRequest request) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found: id=" + id));

        String newName = request.getName().trim();
        categoryRepository.findByNameIgnoreCase(newName).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new ConflictException("Category already exists: " + newName);
            }
        });

        c.setName(newName);
        c.setNameUz(request.getNameUz());
        c.setNameRu(request.getNameRu());
        c.setNameEn(request.getNameEn());
        return CategoryDto.from(categoryRepository.save(c));
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_CATEGORIES, allEntries = true)
    public void delete(Long id) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found: id=" + id));

        if (productRepository.existsByCategoriesId(id)) {
            throw new ConflictException(
                    "Cannot delete category: products are using it. Remove the category from all products first.");
        }

        categoryRepository.delete(c);
    }
}
