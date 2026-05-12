package com.portfolio.silver_lady_s.repository;

import com.portfolio.silver_lady_s.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProductIdOrderByDisplayOrderAscIdAsc(Long productId);

    Optional<ProductImage> findByIdAndProductId(Long id, Long productId);

    int countByProductId(Long productId);

    /** Berilgan mahsulotning barcha rasmlaridan primary belgisini olib tashlaydi */
    @Modifying
    @Query("UPDATE ProductImage pi SET pi.primary = false WHERE pi.product.id = :productId")
    void clearPrimaryByProductId(@Param("productId") Long productId);
}
