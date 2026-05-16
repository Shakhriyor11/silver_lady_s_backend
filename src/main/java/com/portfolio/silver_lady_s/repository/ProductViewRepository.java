package com.portfolio.silver_lady_s.repository;

import com.portfolio.silver_lady_s.entity.ProductView;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ProductViewRepository extends JpaRepository<ProductView, Long> {

    Optional<ProductView> findByUserIdAndProductId(Long userId, Long productId);

    @Query("""
            SELECT cat.id
            FROM ProductView pv
            JOIN pv.product.categories cat
            WHERE pv.user.id = :userId
            GROUP BY cat.id
            ORDER BY SUM(pv.viewCount) DESC
            """)
    List<Long> findTopCategoryIdsByUserId(@Param("userId") Long userId);

    @Query("SELECT pv.product.id FROM ProductView pv WHERE pv.user.id = :userId ORDER BY pv.lastViewedAt DESC")
    List<Long> findViewedProductIdsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Modifying
    @Query("DELETE FROM ProductView pv WHERE pv.lastViewedAt < :cutoff")
    int deleteOlderThan(@Param("cutoff") Instant cutoff);
}
