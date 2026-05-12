package com.portfolio.silver_lady_s.service.impl;

import com.portfolio.silver_lady_s.dto.category.CategoryDto;
import com.portfolio.silver_lady_s.dto.category.CreateCategoryRequest;
import com.portfolio.silver_lady_s.dto.category.UpdateCategoryRequest;
import com.portfolio.silver_lady_s.entity.Category;
import com.portfolio.silver_lady_s.exception.ConflictException;
import com.portfolio.silver_lady_s.exception.NotFoundException;
import com.portfolio.silver_lady_s.repository.CategoryRepository;
import com.portfolio.silver_lady_s.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private ProductRepository productRepository;

    @InjectMocks private CategoryServiceImpl categoryService;

    // ── create ───────────────────────────────────────────────────────────────────

    @Test
    void create_newName_returnsDto() {
        CreateCategoryRequest req = new CreateCategoryRequest();
        req.setName("  Uzuklar  ");

        when(categoryRepository.existsByNameIgnoreCase("Uzuklar")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> {
            Category c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        CategoryDto result = categoryService.create(req);

        assertThat(result.getName()).isEqualTo("Uzuklar");
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void create_duplicateName_throwsConflict() {
        CreateCategoryRequest req = new CreateCategoryRequest();
        req.setName("Uzuklar");

        when(categoryRepository.existsByNameIgnoreCase("Uzuklar")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.create(req))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Uzuklar");
    }

    // ── update ───────────────────────────────────────────────────────────────────

    @Test
    void update_newName_success() {
        Category existing = makeCategory(1L, "Bilakuzuklar");
        UpdateCategoryRequest req = new UpdateCategoryRequest();
        req.setName("Bilaguzuklar");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findByNameIgnoreCase("Bilaguzuklar")).thenReturn(Optional.empty());
        when(categoryRepository.save(existing)).thenReturn(existing);

        CategoryDto result = categoryService.update(1L, req);

        assertThat(result.getName()).isEqualTo("Bilaguzuklar");
    }

    @Test
    void update_sameNameSameId_noConflict() {
        Category existing = makeCategory(1L, "Uzuklar");
        UpdateCategoryRequest req = new UpdateCategoryRequest();
        req.setName("Uzuklar");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        // findByNameIgnoreCase returns same category — should NOT throw
        when(categoryRepository.findByNameIgnoreCase("Uzuklar")).thenReturn(Optional.of(existing));
        when(categoryRepository.save(existing)).thenReturn(existing);

        CategoryDto result = categoryService.update(1L, req);

        assertThat(result.getName()).isEqualTo("Uzuklar");
    }

    @Test
    void update_nameConflictWithDifferentCategory_throwsConflict() {
        Category existing = makeCategory(1L, "Uzuklar");
        Category other   = makeCategory(2L, "Marjonlar");
        UpdateCategoryRequest req = new UpdateCategoryRequest();
        req.setName("Marjonlar");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findByNameIgnoreCase("Marjonlar")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> categoryService.update(1L, req))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Marjonlar");
    }

    @Test
    void update_notFound_throwsNotFoundException() {
        UpdateCategoryRequest req = new UpdateCategoryRequest();
        req.setName("Yangi");

        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.update(99L, req))
                .isInstanceOf(NotFoundException.class);
    }

    // ── delete ───────────────────────────────────────────────────────────────────

    @Test
    void delete_noActiveProducts_deletesCategory() {
        Category c = makeCategory(1L, "Uzuklar");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(c));
        when(productRepository.existsByCategoryIdAndActiveTrue(1L)).thenReturn(false);

        categoryService.delete(1L);

        verify(categoryRepository).delete(c);
    }

    @Test
    void delete_hasActiveProducts_throwsConflict() {
        Category c = makeCategory(1L, "Uzuklar");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(c));
        when(productRepository.existsByCategoryIdAndActiveTrue(1L)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.delete(1L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("active products");

        verify(categoryRepository, never()).delete(any());
    }

    @Test
    void delete_notFound_throwsNotFoundException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.delete(99L))
                .isInstanceOf(NotFoundException.class);
    }

    // ── getById ──────────────────────────────────────────────────────────────────

    @Test
    void getById_found_returnsDto() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(makeCategory(1L, "Uzuklar")));

        CategoryDto result = categoryService.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Uzuklar");
    }

    @Test
    void getById_notFound_throwsNotFoundException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getById(99L))
                .isInstanceOf(NotFoundException.class);
    }

    // ── helpers ──────────────────────────────────────────────────────────────────

    private Category makeCategory(Long id, String name) {
        Category c = new Category();
        c.setId(id);
        c.setName(name);
        return c;
    }
}
