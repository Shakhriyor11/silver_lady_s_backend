package com.portfolio.silver_lady_s.service;

import com.portfolio.silver_lady_s.dto.category.CategoryDto;
import com.portfolio.silver_lady_s.dto.category.CreateCategoryRequest;
import com.portfolio.silver_lady_s.dto.category.UpdateCategoryRequest;

import java.util.List;

public interface CategoryService {
    List<CategoryDto> getAll();
    CategoryDto getById(Long id);
    CategoryDto create(CreateCategoryRequest request);
    CategoryDto update(Long id, UpdateCategoryRequest request);
    void delete(Long id);
}
