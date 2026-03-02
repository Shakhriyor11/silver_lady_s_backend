package com.portfolio.silver_lady_s.service.impl;

import com.portfolio.silver_lady_s.dto.category.CategoryDto;
import com.portfolio.silver_lady_s.dto.category.CreateCategoryRequest;
import com.portfolio.silver_lady_s.dto.category.UpdateCategoryRequest;
import com.portfolio.silver_lady_s.entity.Category;
import com.portfolio.silver_lady_s.exception.ConflictException;
import com.portfolio.silver_lady_s.exception.NotFoundException;
import com.portfolio.silver_lady_s.repository.CategoryRepository;
import com.portfolio.silver_lady_s.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getAll() {
        return categoryRepository.findAll().stream()
                .sorted(Comparator.comparing(Category::getId))
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getById(Long id) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found: id=" + id));
        return toDto(c);
    }

    @Override
    @Transactional
    public CategoryDto create(CreateCategoryRequest request) {
        String name = request.getName().trim();
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Category already exists: " + name);
        }
        Category c = new Category();
        c.setName(name);
        return toDto(categoryRepository.save(c));
    }

    @Override
    @Transactional
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
        return toDto(categoryRepository.save(c));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found: id=" + id));
        categoryRepository.delete(c);
    }

    private CategoryDto toDto(Category c) {
        return new CategoryDto(c.getId(), c.getName());
    }
}
