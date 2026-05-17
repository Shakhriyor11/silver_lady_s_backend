package com.portfolio.silver_lady_s.service.impl;

import com.portfolio.silver_lady_s.dto.product.ProductImageDto;
import com.portfolio.silver_lady_s.entity.Product;
import com.portfolio.silver_lady_s.entity.ProductImage;
import com.portfolio.silver_lady_s.exception.BadRequestException;
import com.portfolio.silver_lady_s.exception.NotFoundException;
import com.portfolio.silver_lady_s.repository.ProductImageRepository;
import com.portfolio.silver_lady_s.repository.ProductRepository;
import com.portfolio.silver_lady_s.service.MediaStorageService;
import com.portfolio.silver_lady_s.service.ProductImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final MediaStorageService mediaStorageService;

    @Value("${app.media.max-images-per-product:10}")
    private int maxImagesPerProduct;

    @Override
    @Transactional
    public List<ProductImageDto> addImages(Long productId, List<MultipartFile> files) {
        Product product = findProduct(productId);

        int current = productImageRepository.countByProductId(productId);
        if (current + files.size() > maxImagesPerProduct) {
            throw new BadRequestException(
                    "Max " + maxImagesPerProduct + " images per product. " +
                    "Current: " + current + ", trying to add: " + files.size());
        }

        boolean isFirstImage = current == 0;
        List<ProductImage> saved = new ArrayList<>();

        for (int i = 0; i < files.size(); i++) {
            String url = mediaStorageService.store(files.get(i), productId);

            ProductImage img = new ProductImage();
            img.setProduct(product);
            img.setUrl(url);
            img.setOriginalFilename(files.get(i).getOriginalFilename());
            img.setFileSize(files.get(i).getSize());
            img.setContentType(files.get(i).getContentType());
            img.setDisplayOrder(current + i);
            // Birinchi yuklangan rasm avtomatik primary bo'ladi
            img.setPrimary(isFirstImage && i == 0);

            saved.add(productImageRepository.save(img));
        }

        return saved.stream().map(ProductImageDto::from).toList();
    }

    @Override
    @Transactional
    public void deleteImage(Long productId, Long imageId) {
        ProductImage img = findImage(productId, imageId);
        boolean wasPrimary = img.isPrimary();
        String url = img.getUrl();

        productImageRepository.delete(img);
        mediaStorageService.delete(url);

        // O'chirilgan rasm primary bo'lsa, birinchi qolgan rasmni primary qilish
        if (wasPrimary) {
            productImageRepository
                    .findByProductIdOrderByDisplayOrderAscIdAsc(productId)
                    .stream()
                    .findFirst()
                    .ifPresent(first -> {
                        first.setPrimary(true);
                        productImageRepository.save(first);
                    });
        }
    }

    @Override
    @Transactional
    public ProductImageDto setPrimary(Long productId, Long imageId) {
        findProduct(productId);
        productImageRepository.clearPrimaryByProductId(productId);

        ProductImage img = findImage(productId, imageId);
        img.setPrimary(true);
        return ProductImageDto.from(productImageRepository.save(img));
    }

    @Override
    @Transactional
    public List<ProductImageDto> reorder(Long productId, List<Long> orderedIds) {
        findProduct(productId);

        List<ProductImage> images =
                productImageRepository.findByProductIdOrderByDisplayOrderAscIdAsc(productId);

        Map<Long, ProductImage> imageMap = images.stream()
                .collect(Collectors.toMap(ProductImage::getId, Function.identity()));

        // Faqat ushbu mahsulotga tegishli ID larni qabul qilamiz
        for (int i = 0; i < orderedIds.size(); i++) {
            Long id = orderedIds.get(i);
            ProductImage img = imageMap.get(id);
            if (img == null) {
                throw new BadRequestException("Image id=" + id + " does not belong to product id=" + productId);
            }
            img.setDisplayOrder(i);
        }

        productImageRepository.saveAll(imageMap.values());

        return productImageRepository
                .findByProductIdOrderByDisplayOrderAscIdAsc(productId)
                .stream()
                .map(ProductImageDto::from)
                .toList();
    }

    // -------------------------------------------------------------------------

    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found: id=" + productId));
    }

    private ProductImage findImage(Long productId, Long imageId) {
        return productImageRepository.findByIdAndProductId(imageId, productId)
                .orElseThrow(() -> new NotFoundException(
                        "Image not found: id=" + imageId + " for product id=" + productId));
    }
}
