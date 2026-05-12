package com.portfolio.silver_lady_s.repository;

import com.portfolio.silver_lady_s.entity.ProductView;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductViewRepository extends JpaRepository<ProductView, Long> {

    Optional<ProductView> findByUserIdAndProductId(Long userId, Long productId);

    /**
     * Foydalanuvchi tomonidan eng ko'p ko'rilgan kategoriyalar ID'lari.
     * Kategoriyalar view_count yig'indisi bo'yicha kamayish tartibida qaytariladi.
     *
     * Bu so'rov RecommendationService da ishlatiladi:
     *   1. Foydalanuvchi ko'p ko'rgan kategoriyalar aniqlanadi.
     *   2. Shu kategoriyalardan yangi va faol mahsulotlar taklif qilinadi.
     */
    @Query("""
            SELECT pv.product.category.id
            FROM ProductView pv
            WHERE pv.user.id = :userId
            GROUP BY pv.product.category.id
            ORDER BY SUM(pv.viewCount) DESC
            """)
    List<Long> findTopCategoryIdsByUserId(@Param("userId") Long userId);

    /**
     * Foydalanuvchi ko'rgan mahsulotlar ID'lari — eng so'nggisi birinchi.
     * Pageable orqali cheklanadi (odatda 200): faol user uchun ham IN (...) kichik bo'ladi.
     */
    @Query("SELECT pv.product.id FROM ProductView pv WHERE pv.user.id = :userId ORDER BY pv.lastViewedAt DESC")
    List<Long> findViewedProductIdsByUserId(@Param("userId") Long userId, Pageable pageable);
}
