package com.portfolio.silver_lady_s.service.impl;

import com.portfolio.silver_lady_s.dto.product.CreateProductRequest;
import com.portfolio.silver_lady_s.dto.product.ProductDto;
import com.portfolio.silver_lady_s.dto.product.UpdateProductRequest;
import com.portfolio.silver_lady_s.entity.Category;
import com.portfolio.silver_lady_s.entity.Product;
import com.portfolio.silver_lady_s.exception.NotFoundException;
import com.portfolio.silver_lady_s.repository.CategoryRepository;
import com.portfolio.silver_lady_s.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;

    @InjectMocks private ProductServiceImpl productService;

    private Category category;
    private Product product;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Uzuklar");

        product = new Product();
        product.setId(10L);
        product.setName("Oltin uzuk");
        product.setDescription("24 karat oltin");
        product.setPrice(new BigDecimal("500000.00"));
        product.setCategory(category);
        product.setActive(true);
    }

    // ── getById ──────────────────────────────────────────────────────────────────

    @Test
    void getById_activeProduct_returnsDto() {
        when(productRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(product));

        ProductDto result = productService.getById(10L);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo("Oltin uzuk");
        assertThat(result.getPrice()).isEqualByComparingTo("500000.00");
        assertThat(result.getCategoryId()).isEqualTo(1L);
        assertThat(result.isActive()).isTrue();
    }

    @Test
    void getById_notFound_throwsNotFoundException() {
        when(productRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getById(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    // ── create ───────────────────────────────────────────────────────────────────

    @Test
    void create_success_returnsDto() {
        CreateProductRequest req = new CreateProductRequest();
        req.setName("  Kumush uzuk  ");
        req.setDescription("Sterling kumush");
        req.setPrice(new BigDecimal("120000.00"));
        req.setCategoryId(1L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setId(11L);
            return p;
        });

        ProductDto result = productService.create(req);

        assertThat(result.getName()).isEqualTo("Kumush uzuk"); // trimmed
        assertThat(result.getId()).isEqualTo(11L);
    }

    @Test
    void create_categoryNotFound_throwsNotFoundException() {
        CreateProductRequest req = new CreateProductRequest();
        req.setName("Uzuk");
        req.setPrice(new BigDecimal("100000.00"));
        req.setCategoryId(99L);

        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.create(req))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    // ── update ───────────────────────────────────────────────────────────────────

    @Test
    void update_success() {
        UpdateProductRequest req = new UpdateProductRequest();
        req.setName("Yangilangan uzuk");
        req.setPrice(new BigDecimal("600000.00"));
        req.setCategoryId(1L);

        when(productRepository.findWithCategoryById(10L)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(product)).thenReturn(product);

        ProductDto result = productService.update(10L, req);

        assertThat(result.getName()).isEqualTo("Yangilangan uzuk");
        assertThat(result.getPrice()).isEqualByComparingTo("600000.00");
    }

    @Test
    void update_productNotFound_throwsNotFoundException() {
        UpdateProductRequest req = new UpdateProductRequest();
        req.setName("X");
        req.setPrice(BigDecimal.ONE);
        req.setCategoryId(1L);

        when(productRepository.findWithCategoryById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update(99L, req))
                .isInstanceOf(NotFoundException.class);
    }

    // ── delete (soft) ─────────────────────────────────────────────────────────────

    @Test
    void delete_setsActiveFalse() {
        when(productRepository.findWithCategoryById(10L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        productService.delete(10L);

        assertThat(product.isActive()).isFalse();
        verify(productRepository).save(product);
    }

    @Test
    void delete_notFound_throwsNotFoundException() {
        when(productRepository.findWithCategoryById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.delete(99L))
                .isInstanceOf(NotFoundException.class);
    }

    // ── restore ──────────────────────────────────────────────────────────────────

    @Test
    void restore_inactiveProduct_setsActiveTrue() {
        product.setActive(false);
        when(productRepository.findByIdAndActiveFalse(10L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        ProductDto result = productService.restore(10L);

        assertThat(product.isActive()).isTrue();
        assertThat(result.isActive()).isTrue();
    }

    @Test
    void restore_activeProduct_throwsNotFoundException() {
        // findByIdAndActiveFalse returns empty when product is active
        when(productRepository.findByIdAndActiveFalse(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.restore(10L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Inactive product");
    }

    // ── getSimilarProducts ────────────────────────────────────────────────────────

    @Test
    void getSimilarProducts_productNotFound_throwsNotFoundException() {
        when(productRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getSimilarProducts(99L, 5))
                .isInstanceOf(NotFoundException.class);
    }
}
