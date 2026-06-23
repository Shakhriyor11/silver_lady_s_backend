package com.portfolio.silver_lady_s.service.impl;

import com.portfolio.silver_lady_s.config.CacheConfig;
import com.portfolio.silver_lady_s.dto.category.CategoryDto;
import com.portfolio.silver_lady_s.dto.category.CreateCategoryRequest;
import com.portfolio.silver_lady_s.dto.category.UpdateCategoryRequest;
import com.portfolio.silver_lady_s.entity.Category;
import com.portfolio.silver_lady_s.exception.BadRequestException;
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
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(CacheConfig.CACHE_CATEGORIES)
    public List<CategoryDto> getAll() {
        return categoryRepository.findAllByParentIsNullOrderBySortOrderAscIdAsc()
                .stream()
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
        Long parentId = request.getParentId();

        checkNameUnique(name, parentId, null);

        Category c = new Category();
        c.setName(name);
        c.setNameUz(request.getNameUz());
        c.setNameRu(request.getNameRu());
        c.setNameEn(request.getNameEn());

        if (parentId != null) {
            Category parent = categoryRepository.findById(parentId)
                    .orElseThrow(() -> new NotFoundException("Parent category not found: id=" + parentId));
            if (parent.getParent() != null) {
                throw new BadRequestException("Nested subcategories beyond 2 levels are not allowed");
            }
            c.setParent(parent);
        }

        // sort_order = mavjud kategoriyalar sonidan keyin (oxiriga qo'shilsin)
        c.setSortOrder((int) categoryRepository.count());

        return CategoryDto.from(categoryRepository.save(c));
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_CATEGORIES, allEntries = true)
    public CategoryDto update(Long id, UpdateCategoryRequest request) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found: id=" + id));

        String newName = request.getName().trim();
        Long parentId = request.getParentId();

        checkNameUnique(newName, parentId, id);

        c.setName(newName);
        c.setNameUz(request.getNameUz());
        c.setNameRu(request.getNameRu());
        c.setNameEn(request.getNameEn());

        if (parentId == null) {
            c.setParent(null);
        } else {
            if (parentId.equals(id)) {
                throw new BadRequestException("A category cannot be its own parent");
            }
            Category parent = categoryRepository.findById(parentId)
                    .orElseThrow(() -> new NotFoundException("Parent category not found: id=" + parentId));
            if (parent.getParent() != null) {
                throw new BadRequestException("Nested subcategories beyond 2 levels are not allowed");
            }
            c.setParent(parent);
        }

        return CategoryDto.from(categoryRepository.save(c));
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_CATEGORIES, allEntries = true)
    public void delete(Long id) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found: id=" + id));

        if (!c.getChildren().isEmpty()) {
            throw new ConflictException(
                    "Cannot delete category: it has subcategories. Delete subcategories first.");
        }

        if (productRepository.existsByCategoriesId(id)) {
            throw new ConflictException(
                    "Cannot delete category: products are using it. Remove the category from all products first.");
        }

        categoryRepository.delete(c);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.CACHE_CATEGORIES, allEntries = true)
    public void reorder(List<Long> orderedIds) {
        if (orderedIds == null || orderedIds.isEmpty()) {
            throw new BadRequestException("orderedIds must not be empty");
        }

        List<Category> categories = categoryRepository.findAllById(orderedIds);
        Map<Long, Category> byId = categories.stream()
                .collect(Collectors.toMap(Category::getId, cat -> cat));

        for (int i = 0; i < orderedIds.size(); i++) {
            Long catId = orderedIds.get(i);
            Category cat = byId.get(catId);
            if (cat == null) {
                throw new NotFoundException("Category not found: id=" + catId);
            }
            cat.setSortOrder(i);
        }

        categoryRepository.saveAll(categories);
    }

    // ─────────────────────────────────────────────────────────────────────────

    private void checkNameUnique(String name, Long parentId, Long excludeId) {
        if (parentId == null) {
            categoryRepository.findByNameIgnoreCaseAndParentIsNull(name).ifPresent(existing -> {
                if (!existing.getId().equals(excludeId)) {
                    throw new ConflictException("Category already exists: " + name);
                }
            });
        } else {
            categoryRepository.findByNameIgnoreCaseAndParentId(name, parentId).ifPresent(existing -> {
                if (!existing.getId().equals(excludeId)) {
                    throw new ConflictException("Category already exists under this parent: " + name);
                }
            });
        }
    }
}
