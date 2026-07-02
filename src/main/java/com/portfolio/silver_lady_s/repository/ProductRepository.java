package com.portfolio.silver_lady_s.repository;

import com.portfolio.silver_lady_s.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductFilterRepository {

    // ── Simple list views (no filter — single-step, no JOIN) ─────────────────

    @EntityGraph(attributePaths = "categories")
    Page<Product> findAllByActiveTrueOrderByIdDesc(Pageable pageable);

    @EntityGraph(attributePaths = "categories")
    Page<Product> findAllByActiveTrueOrderByPriceAsc(Pageable pageable);

    @EntityGraph(attributePaths = "categories")
    Page<Product> findAllByActiveTrueOrderByPriceDesc(Pageable pageable);

    @EntityGraph(attributePaths = "categories")
    Page<Product> findAllByActiveFalseOrderByIdDesc(Pageable pageable);

    // ── Checkout: pessimistic lock (SELECT … FOR UPDATE) ─────────────────────

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id IN :ids")
    List<Product> findAllByIdsForUpdate(@Param("ids") List<Long> ids);

    // ── Step-2 fetch with JOIN FETCH (used after ID pagination) ──────────────

    @Query("""
            SELECT DISTINCT p FROM Product p
            LEFT JOIN FETCH p.categories
            WHERE p.id IN :ids
            """)
    List<Product> findByIdsWithDetails(@Param("ids") List<Long> ids);

    // ── Single product fetches ────────────────────────────────────────────────

    @Query("""
            SELECT DISTINCT p FROM Product p
            LEFT JOIN FETCH p.categories
            WHERE p.id = :id AND p.active = true
            """)
    Optional<Product> findByIdAndActiveTrueWithImages(@Param("id") Long id);

    @Query("""
            SELECT DISTINCT p FROM Product p
            LEFT JOIN FETCH p.categories
            WHERE p.id = :id
            """)
    Optional<Product> findByIdWithImages(@Param("id") Long id);

    @EntityGraph(attributePaths = "categories")
    Optional<Product> findByIdAndActiveTrue(Long id);

    @EntityGraph(attributePaths = "categories")
    Optional<Product> findByIdAndActiveFalse(Long id);

    @EntityGraph(attributePaths = "categories")
    Optional<Product> findWithCategoryById(Long id);

    @EntityGraph(attributePaths = {"categories", "sizeEntries"})
    @Query("SELECT p FROM Product p WHERE p.id = :id AND p.active = true")
    Optional<Product> findByIdAndActiveTrueForCart(@Param("id") Long id);

    // ── Existence checks ──────────────────────────────────────────────────────

    boolean existsByCategoriesId(Long categoryId);

    // ── Similar & Recommendation queries ─────────────────────────────────────

    @Query("""
            SELECT DISTINCT p FROM Product p
            JOIN FETCH p.categories cats
            WHERE cats.id IN :categoryIds
              AND p.id <> :excludeId
              AND p.active = true
            ORDER BY p.id DESC
            """)
    List<Product> findSimilar(@Param("categoryIds") List<Long> categoryIds,
                              @Param("excludeId") Long excludeId,
                              Pageable pageable);

    @Query("""
            SELECT DISTINCT p FROM Product p
            JOIN p.categories cat
            WHERE cat.id IN :categoryIds
              AND p.id NOT IN :excludeIds
              AND p.active = true
            ORDER BY p.id DESC
            """)
    List<Product> findByCategoryIdsExcluding(
            @Param("categoryIds") List<Long> categoryIds,
            @Param("excludeIds") List<Long> excludeIds,
            Pageable pageable);

    @Query("""
            SELECT p FROM Product p
            WHERE p.active = true
              AND p.id NOT IN :excludeIds
            ORDER BY p.id DESC
            """)
    List<Product> findActiveExcluding(
            @Param("excludeIds") List<Long> excludeIds,
            Pageable pageable);

    // ── Distinct sizes (for filter UI) ───────────────────────────────────────

    @Query(value = """
            SELECT ps.size, COUNT(DISTINCT p.id) AS cnt
            FROM product_size_stock ps
            JOIN products p ON p.id = ps.product_id
            WHERE p.active = true AND ps.quantity > 0
            GROUP BY ps.size
            ORDER BY ps.size
            """, nativeQuery = true)
    List<Object[]> findSizesWithCounts();

    @Query(value = """
            SELECT ps.size, COUNT(DISTINCT p.id) AS cnt
            FROM product_size_stock ps
            JOIN products p ON p.id = ps.product_id
            JOIN product_categories pc ON pc.product_id = p.id
            WHERE p.active = true AND ps.quantity > 0
              AND pc.category_id = :categoryId
            GROUP BY ps.size
            ORDER BY ps.size
            """, nativeQuery = true)
    List<Object[]> findSizesWithCountsByCategory(@Param("categoryId") Long categoryId);
}
