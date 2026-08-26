package com.portfolio.silver_lady_s.repository;

import com.portfolio.silver_lady_s.entity.WishlistItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    @EntityGraph(attributePaths = {"product", "product.categories", "product.images"})
    List<WishlistItem> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT wi.product.id FROM WishlistItem wi WHERE wi.user.id = :userId")
    Set<Long> findProductIdsByUserId(@Param("userId") Long userId);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    void deleteByUserIdAndProductId(Long userId, Long productId);
}
