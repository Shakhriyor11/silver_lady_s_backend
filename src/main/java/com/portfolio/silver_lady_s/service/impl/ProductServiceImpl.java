package com.portfolio.silver_lady_s.service.impl;

import com.portfolio.silver_lady_s.dto.PageResponse;
import com.portfolio.silver_lady_s.dto.product.CreateProductRequest;
import com.portfolio.silver_lady_s.dto.product.ProductDto;
import com.portfolio.silver_lady_s.dto.product.SizeCountDto;
import com.portfolio.silver_lady_s.dto.product.SizeEntryRequest;
import com.portfolio.silver_lady_s.dto.product.UpdateProductRequest;
import com.portfolio.silver_lady_s.entity.Category;
import com.portfolio.silver_lady_s.entity.Product;
import com.portfolio.silver_lady_s.entity.ProductSizeEntry;
import com.portfolio.silver_lady_s.exception.NotFoundException;
import com.portfolio.silver_lady_s.repository.CategoryRepository;
import com.portfolio.silver_lady_s.repository.ProductRepository;
import com.portfolio.silver_lady_s.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductDto> getProducts(Long categoryId, String search, String sort,
                                                String sizeFilter, Pageable pageable) {
        String q         = StringUtils.hasText(search) ? search.trim() : null;
        boolean priceAsc  = "price_asc".equals(sort);
        boolean priceDesc = "price_desc".equals(sort);
        boolean hasSize   = StringUtils.hasText(sizeFilter);
        boolean hasCat    = categoryId != null;

        if (StringUtils.hasText(q)) {
            String pattern = "%" + q + "%";
            Page<Long> idPage;
            if (hasCat && hasSize) {
                idPage = priceAsc  ? productRepository.searchActiveByCategoryIdsAndSizePriceAsc(q, pattern, categoryId, sizeFilter, pageable)
                       : priceDesc ? productRepository.searchActiveByCategoryIdsAndSizePriceDesc(q, pattern, categoryId, sizeFilter, pageable)
                       :             productRepository.searchActiveByCategoryIdsAndSize(q, pattern, categoryId, sizeFilter, pageable);
            } else if (hasCat) {
                idPage = priceAsc  ? productRepository.searchActiveByCategoryIdsPriceAsc(q, pattern, categoryId, pageable)
                       : priceDesc ? productRepository.searchActiveByCategoryIdsPriceDesc(q, pattern, categoryId, pageable)
                       :             productRepository.searchActiveByCategoryIds(q, pattern, categoryId, pageable);
            } else if (hasSize) {
                idPage = priceAsc  ? productRepository.searchActiveIdsAndSizePriceAsc(q, pattern, sizeFilter, pageable)
                       : priceDesc ? productRepository.searchActiveIdsAndSizePriceDesc(q, pattern, sizeFilter, pageable)
                       :             productRepository.searchActiveIdsAndSize(q, pattern, sizeFilter, pageable);
            } else {
                idPage = priceAsc  ? productRepository.searchActiveIdsPriceAsc(q, pattern, pageable)
                       : priceDesc ? productRepository.searchActiveIdsPriceDesc(q, pattern, pageable)
                       :             productRepository.searchActiveIds(q, pattern, pageable);
            }
            return fetchByIds(idPage, pageable);
        }

        if (hasCat || hasSize) {
            Page<Long> idPage;
            if (hasCat && hasSize) {
                idPage = priceAsc  ? productRepository.findIdsByCategoryActiveAndSizePriceAsc(categoryId, sizeFilter, pageable)
                       : priceDesc ? productRepository.findIdsByCategoryActiveAndSizePriceDesc(categoryId, sizeFilter, pageable)
                       :             productRepository.findIdsByCategoryActiveAndSize(categoryId, sizeFilter, pageable);
            } else if (hasCat) {
                idPage = priceAsc  ? productRepository.findIdsByCategoryActivePriceAsc(categoryId, pageable)
                       : priceDesc ? productRepository.findIdsByCategoryActivePriceDesc(categoryId, pageable)
                       :             productRepository.findIdsByCategoryActive(categoryId, pageable);
            } else {
                idPage = priceAsc  ? productRepository.findIdsByActiveAndSizePriceAsc(sizeFilter, pageable)
                       : priceDesc ? productRepository.findIdsByActiveAndSizePriceDesc(sizeFilter, pageable)
                       :             productRepository.findIdsByActiveAndSize(sizeFilter, pageable);
            }
            return fetchByIds(idPage, pageable);
        }

        Page<Product> page = priceAsc  ? productRepository.findAllByActiveTrueOrderByPriceAsc(pageable)
                           : priceDesc ? productRepository.findAllByActiveTrueOrderByPriceDesc(pageable)
                           :             productRepository.findAllByActiveTrueOrderByIdDesc(pageable);
        return new PageResponse<>(page.map(ProductDto::from));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SizeCountDto> getAvailableSizes() {
        return productRepository.findSizesWithCounts().stream()
                .map(row -> new SizeCountDto((String) row[0], ((Number) row[1]).longValue()))
                .toList();
    }

    private PageResponse<ProductDto> fetchByIds(Page<Long> idPage, Pageable pageable) {
        List<Long> ids = idPage.getContent();
        if (ids.isEmpty()) return new PageResponse<>(new PageImpl<>(List.of(), pageable, 0));
        Map<Long, Product> byId = productRepository.findByIdsWithDetails(ids).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
        List<ProductDto> dtos = ids.stream()
                .map(byId::get).filter(Objects::nonNull).map(ProductDto::from).toList();
        return new PageResponse<>(new PageImpl<>(dtos, pageable, idPage.getTotalElements()));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductDto> getArchivedProducts(Pageable pageable) {
        return new PageResponse<>(
                productRepository.findAllByActiveFalseOrderByIdDesc(pageable).map(ProductDto::from));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto getById(Long id) {
        return ProductDto.from(productRepository.findByIdAndActiveTrueWithImages(id)
                .orElseThrow(() -> new NotFoundException("Product not found: id=" + id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> getSimilarProducts(Long productId, int limit) {
        Product product = productRepository.findByIdAndActiveTrue(productId)
                .orElseThrow(() -> new NotFoundException("Product not found: id=" + productId));

        List<Long> categoryIds = product.getCategories().stream()
                .map(Category::getId)
                .toList();

        if (categoryIds.isEmpty()) return List.of();

        return productRepository
                .findSimilar(categoryIds, productId, PageRequest.of(0, limit))
                .stream()
                .map(ProductDto::from)
                .toList();
    }

    @Override
    @Transactional
    public ProductDto create(CreateProductRequest req) {
        List<Category> cats = resolveCategories(req.getCategoryIds());

        Product p = new Product();
        p.setName(req.getName().trim());
        p.setNameUz(req.getNameUz());
        p.setNameRu(req.getNameRu());
        p.setNameEn(req.getNameEn());
        p.setDescription(req.getDescription());
        p.setDescriptionUz(req.getDescriptionUz());
        p.setDescriptionRu(req.getDescriptionRu());
        p.setDescriptionEn(req.getDescriptionEn());
        p.setPrice(req.getPrice());
        p.setDiscountPercent(req.getDiscountPercent());
        p.setDiscountAmount(req.getDiscountAmount());
        p.setDiscountStartsAt(req.getDiscountStartsAt());
        p.setDiscountEndsAt(req.getDiscountEndsAt());
        p.getCategories().addAll(cats);
        if (req.getActive() != null) p.setActive(req.getActive());

        if (req.getSizeEntries() != null && !req.getSizeEntries().isEmpty()) {
            for (int i = 0; i < req.getSizeEntries().size(); i++) {
                SizeEntryRequest ser = req.getSizeEntries().get(i);
                ProductSizeEntry entry = new ProductSizeEntry();
                entry.setProduct(p);
                entry.setSize(ser.size());
                entry.setQuantity(ser.quantity());
                entry.setSortOrder(i);
                p.getSizeEntries().add(entry);
            }
            p.setStockQuantity(req.getSizeEntries().stream().mapToInt(SizeEntryRequest::quantity).sum());
        } else {
            p.setStockQuantity(req.getStockQuantity() != null ? req.getStockQuantity() : 0);
        }

        return ProductDto.from(productRepository.save(p));
    }

    @Override
    @Transactional
    public ProductDto update(Long id, UpdateProductRequest req) {
        Product p = productRepository.findWithCategoryById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: id=" + id));

        List<Category> cats = resolveCategories(req.getCategoryIds());

        p.setName(req.getName().trim());
        p.setNameUz(req.getNameUz());
        p.setNameRu(req.getNameRu());
        p.setNameEn(req.getNameEn());
        p.setDescription(req.getDescription());
        p.setDescriptionUz(req.getDescriptionUz());
        p.setDescriptionRu(req.getDescriptionRu());
        p.setDescriptionEn(req.getDescriptionEn());
        p.setPrice(req.getPrice());
        p.setDiscountPercent(req.getDiscountPercent());
        p.setDiscountAmount(req.getDiscountAmount());
        p.setDiscountStartsAt(req.getDiscountStartsAt());
        p.setDiscountEndsAt(req.getDiscountEndsAt());
        p.getCategories().clear();
        p.getCategories().addAll(cats);
        if (req.getActive() != null) p.setActive(req.getActive());

        if (req.getSizeEntries() != null) {
            mergeSizeEntries(p, req.getSizeEntries());
            p.setStockQuantity(req.getSizeEntries().stream().mapToInt(SizeEntryRequest::quantity).sum());
        } else if (req.getStockQuantity() != null) {
            p.setStockQuantity(req.getStockQuantity());
        }

        return ProductDto.from(productRepository.save(p));
    }

    private void mergeSizeEntries(Product p, List<SizeEntryRequest> requested) {
        Map<String, ProductSizeEntry> currentBySize =
                p.getSizeEntries().stream()
                        .collect(Collectors.toMap(
                                ProductSizeEntry::getSize, e -> e));

        Set<String> requestedSizes = requested.stream()
                .map(SizeEntryRequest::size)
                .collect(Collectors.toSet());

        p.getSizeEntries().removeIf(e -> !requestedSizes.contains(e.getSize()));

        for (int i = 0; i < requested.size(); i++) {
            SizeEntryRequest ser = requested.get(i);
            if (currentBySize.containsKey(ser.size())) {
                ProductSizeEntry existing = currentBySize.get(ser.size());
                existing.setQuantity(ser.quantity());
                existing.setSortOrder(i);
            } else {
                ProductSizeEntry entry = new ProductSizeEntry();
                entry.setProduct(p);
                entry.setSize(ser.size());
                entry.setQuantity(ser.quantity());
                entry.setSortOrder(i);
                p.getSizeEntries().add(entry);
            }
        }
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

    // ─────────────────────────────────────────────────────────────────────────

    private List<Category> resolveCategories(List<Long> ids) {
        List<Category> cats = categoryRepository.findAllById(ids);
        if (cats.size() != ids.size()) {
            Set<Long> foundSet = cats.stream().map(Category::getId).collect(Collectors.toSet());
            List<Long> missing = ids.stream().filter(id -> !foundSet.contains(id)).toList();
            throw new NotFoundException("Categories not found: " + missing);
        }
        return cats;
    }
}
