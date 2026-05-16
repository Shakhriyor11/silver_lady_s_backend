package com.portfolio.silver_lady_s.controller;

import com.portfolio.silver_lady_s.dto.PageResponse;
import com.portfolio.silver_lady_s.dto.product.CreateProductRequest;
import com.portfolio.silver_lady_s.dto.product.ProductDto;
import com.portfolio.silver_lady_s.dto.product.UpdateProductRequest;
import com.portfolio.silver_lady_s.security.UserPrincipal;
import com.portfolio.silver_lady_s.service.ProductService;
import com.portfolio.silver_lady_s.service.ProductViewService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Validated
public class ProductController {

    private final ProductService productService;
    private final ProductViewService productViewService;

    @GetMapping
    public PageResponse<ProductDto> getProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0")  @Min(0)          int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return productService.getProducts(categoryId, search, pageable);
    }

    @GetMapping("/archived")
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<ProductDto> getArchived(
            @RequestParam(defaultValue = "0")  @Min(0)          int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return productService.getArchivedProducts(PageRequest.of(page, size));
    }

    @GetMapping("/{id}")
    public ProductDto getById(@PathVariable Long id, Authentication authentication) {
        ProductDto dto = productService.getById(id);
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal up) {
            productViewService.recordView(up.getUserId(), id);
        }
        return dto;
    }

    @GetMapping("/{id}/similar")
    public List<ProductDto> getSimilar(
            @PathVariable Long id,
            @RequestParam(defaultValue = "6") @Min(1) @Max(20) int limit
    ) {
        return productService.getSimilarProducts(id, limit);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDto> create(@Valid @RequestBody CreateProductRequest req) {
        ProductDto created = productService.create(req);
        return ResponseEntity.created(URI.create("/api/products/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductDto update(@PathVariable Long id, @Valid @RequestBody UpdateProductRequest req) {
        return productService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductDto restore(@PathVariable Long id) {
        return productService.restore(id);
    }
}
