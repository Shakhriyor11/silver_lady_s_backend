package com.portfolio.silver_lady_s.repository;

import com.portfolio.silver_lady_s.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = "category")
    Page<Product> findAllByActiveTrueOrderByIdDesc(Pageable pageable);

    @EntityGraph(attributePaths = "category")
    Page<Product> findByCategoryIdAndActiveTrueOrderByIdDesc(Long categoryId, Pageable pageable);

    @EntityGraph(attributePaths = "category")
    Page<Product> findByNameContainingIgnoreCaseAndActiveTrueOrderByIdDesc(String name, Pageable pageable);

    @EntityGraph(attributePaths = "category")
    Page<Product> findByCategoryIdAndNameContainingIgnoreCaseAndActiveTrueOrderByIdDesc(
            Long categoryId, String name, Pageable pageable);

    @EntityGraph(attributePaths = "category")
    Optional<Product> findByIdAndActiveTrue(Long id);

    boolean existsByCategoryIdAndActiveTrue(Long categoryId);

    /** Admin: o'chirilgan (active=false) mahsulotni topish uchun */
    @EntityGraph(attributePaths = "category")
    Optional<Product> findByIdAndActiveFalse(Long id);

    @EntityGraph(attributePaths = "category")
    Optional<Product> findWithCategoryById(Long id);

    @EntityGraph(attributePaths = "category")
    @Query("""
            SELECT p FROM Product p
            WHERE p.category.id = :categoryId
              AND p.id <> :excludeId
              AND p.active = true
            ORDER BY p.id DESC
            """)
    List<Product> findSimilar(@Param("categoryId") Long categoryId,
                              @Param("excludeId") Long excludeId,
                              Pageable pageable);

    @EntityGraph(attributePaths = "category")
    @Query("""
            SELECT p FROM Product p
            WHERE p.category.id IN :categoryIds
              AND p.id NOT IN :excludeIds
              AND p.active = true
            ORDER BY p.id DESC
            """)
    List<Product> findByCategoryIdsExcluding(
            @Param("categoryIds") List<Long> categoryIds,
            @Param("excludeIds") List<Long> excludeIds,
            Pageable pageable);

    @EntityGraph(attributePaths = "category")
    @Query("""
            SELECT p FROM Product p
            WHERE p.active = true
              AND p.id NOT IN :excludeIds
            ORDER BY p.id DESC
            """)
    List<Product> findActiveExcluding(
            @Param("excludeIds") List<Long> excludeIds,
            Pageable pageable);
}
