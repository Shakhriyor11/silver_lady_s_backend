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

    // ── List views (batch-fetch categories via default_batch_fetch_size) ──────

    Page<Product> findAllByActiveTrueOrderByIdDesc(Pageable pageable);

    Page<Product> findAllByActiveTrueOrderByPriceAsc(Pageable pageable);

    Page<Product> findAllByActiveTrueOrderByPriceDesc(Pageable pageable);

    Page<Product> findAllByActiveFalseOrderByIdDesc(Pageable pageable);

    // ── Category-filtered list (two-step: native IDs → JPQL fetch) ───────────

    @Query(value = """
            SELECT p.id FROM products p
            JOIN product_categories pc ON pc.product_id = p.id
            WHERE p.active = true
              AND pc.category_id = :categoryId
            ORDER BY p.id DESC
            """,
            countQuery = """
            SELECT count(DISTINCT p.id) FROM products p
            JOIN product_categories pc ON pc.product_id = p.id
            WHERE p.active = true
              AND pc.category_id = :categoryId
            """,
            nativeQuery = true)
    Page<Long> findIdsByCategoryActive(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query(value = """
            SELECT p.id FROM products p
            JOIN product_categories pc ON pc.product_id = p.id
            WHERE p.active = true
              AND pc.category_id = :categoryId
            ORDER BY CASE
              WHEN (p.discount_starts_at IS NULL OR p.discount_starts_at <= NOW())
               AND (p.discount_ends_at   IS NULL OR p.discount_ends_at   >  NOW())
               AND p.discount_percent IS NOT NULL AND p.discount_percent > 0
                THEN p.price * (1 - p.discount_percent / 100.0)
              WHEN (p.discount_starts_at IS NULL OR p.discount_starts_at <= NOW())
               AND (p.discount_ends_at   IS NULL OR p.discount_ends_at   >  NOW())
               AND p.discount_amount IS NOT NULL AND p.discount_amount > 0
                THEN GREATEST(0, p.price - p.discount_amount)
              ELSE p.price
            END ASC
            """,
            countQuery = """
            SELECT count(DISTINCT p.id) FROM products p
            JOIN product_categories pc ON pc.product_id = p.id
            WHERE p.active = true
              AND pc.category_id = :categoryId
            """,
            nativeQuery = true)
    Page<Long> findIdsByCategoryActivePriceAsc(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query(value = """
            SELECT p.id FROM products p
            JOIN product_categories pc ON pc.product_id = p.id
            WHERE p.active = true
              AND pc.category_id = :categoryId
            ORDER BY CASE
              WHEN (p.discount_starts_at IS NULL OR p.discount_starts_at <= NOW())
               AND (p.discount_ends_at   IS NULL OR p.discount_ends_at   >  NOW())
               AND p.discount_percent IS NOT NULL AND p.discount_percent > 0
                THEN p.price * (1 - p.discount_percent / 100.0)
              WHEN (p.discount_starts_at IS NULL OR p.discount_starts_at <= NOW())
               AND (p.discount_ends_at   IS NULL OR p.discount_ends_at   >  NOW())
               AND p.discount_amount IS NOT NULL AND p.discount_amount > 0
                THEN GREATEST(0, p.price - p.discount_amount)
              ELSE p.price
            END DESC
            """,
            countQuery = """
            SELECT count(DISTINCT p.id) FROM products p
            JOIN product_categories pc ON pc.product_id = p.id
            WHERE p.active = true
              AND pc.category_id = :categoryId
            """,
            nativeQuery = true)
    Page<Long> findIdsByCategoryActivePriceDesc(@Param("categoryId") Long categoryId, Pageable pageable);

    // ── Full-text search (two-step: native IDs → JPQL fetch) ─────────────────

    @Query(value = """
            SELECT p.id FROM products p
            WHERE p.active = true
              AND (
                p.name           ILIKE :pattern
                OR p.description ILIKE :pattern
                OR word_similarity(:query, p.name)                      > 0.3
                OR word_similarity(:query, COALESCE(p.description,''))  > 0.3
              )
            ORDER BY
              GREATEST(
                word_similarity(:query, p.name),
                word_similarity(:query, COALESCE(p.description,''))
              ) DESC,
              p.id DESC
            """,
            countQuery = """
            SELECT count(*) FROM products p
            WHERE p.active = true
              AND (
                p.name           ILIKE :pattern
                OR p.description ILIKE :pattern
                OR word_similarity(:query, p.name)                      > 0.3
                OR word_similarity(:query, COALESCE(p.description,''))  > 0.3
              )
            """,
            nativeQuery = true)
    Page<Long> searchActiveIds(@Param("query") String query,
                               @Param("pattern") String pattern,
                               Pageable pageable);

    @Query(value = """
            SELECT p.id FROM products p
            WHERE p.active = true
              AND (
                p.name           ILIKE :pattern
                OR p.description ILIKE :pattern
                OR word_similarity(:query, p.name)                      > 0.3
                OR word_similarity(:query, COALESCE(p.description,''))  > 0.3
              )
            ORDER BY CASE
              WHEN (p.discount_starts_at IS NULL OR p.discount_starts_at <= NOW())
               AND (p.discount_ends_at   IS NULL OR p.discount_ends_at   >  NOW())
               AND p.discount_percent IS NOT NULL AND p.discount_percent > 0
                THEN p.price * (1 - p.discount_percent / 100.0)
              WHEN (p.discount_starts_at IS NULL OR p.discount_starts_at <= NOW())
               AND (p.discount_ends_at   IS NULL OR p.discount_ends_at   >  NOW())
               AND p.discount_amount IS NOT NULL AND p.discount_amount > 0
                THEN GREATEST(0, p.price - p.discount_amount)
              ELSE p.price
            END ASC
            """,
            countQuery = """
            SELECT count(*) FROM products p
            WHERE p.active = true
              AND (
                p.name           ILIKE :pattern
                OR p.description ILIKE :pattern
                OR word_similarity(:query, p.name)                      > 0.3
                OR word_similarity(:query, COALESCE(p.description,''))  > 0.3
              )
            """,
            nativeQuery = true)
    Page<Long> searchActiveIdsPriceAsc(@Param("query") String query,
                                       @Param("pattern") String pattern,
                                       Pageable pageable);

    @Query(value = """
            SELECT p.id FROM products p
            WHERE p.active = true
              AND (
                p.name           ILIKE :pattern
                OR p.description ILIKE :pattern
                OR word_similarity(:query, p.name)                      > 0.3
                OR word_similarity(:query, COALESCE(p.description,''))  > 0.3
              )
            ORDER BY CASE
              WHEN (p.discount_starts_at IS NULL OR p.discount_starts_at <= NOW())
               AND (p.discount_ends_at   IS NULL OR p.discount_ends_at   >  NOW())
               AND p.discount_percent IS NOT NULL AND p.discount_percent > 0
                THEN p.price * (1 - p.discount_percent / 100.0)
              WHEN (p.discount_starts_at IS NULL OR p.discount_starts_at <= NOW())
               AND (p.discount_ends_at   IS NULL OR p.discount_ends_at   >  NOW())
               AND p.discount_amount IS NOT NULL AND p.discount_amount > 0
                THEN GREATEST(0, p.price - p.discount_amount)
              ELSE p.price
            END DESC
            """,
            countQuery = """
            SELECT count(*) FROM products p
            WHERE p.active = true
              AND (
                p.name           ILIKE :pattern
                OR p.description ILIKE :pattern
                OR word_similarity(:query, p.name)                      > 0.3
                OR word_similarity(:query, COALESCE(p.description,''))  > 0.3
              )
            """,
            nativeQuery = true)
    Page<Long> searchActiveIdsPriceDesc(@Param("query") String query,
                                        @Param("pattern") String pattern,
                                        Pageable pageable);

    @Query(value = """
            SELECT p.id FROM products p
            WHERE p.active = true
              AND EXISTS (
                  SELECT 1 FROM product_categories pc
                  WHERE pc.product_id = p.id AND pc.category_id = :categoryId
              )
              AND (
                p.name           ILIKE :pattern
                OR p.description ILIKE :pattern
                OR word_similarity(:query, p.name)                      > 0.3
                OR word_similarity(:query, COALESCE(p.description,''))  > 0.3
              )
            ORDER BY
              GREATEST(
                word_similarity(:query, p.name),
                word_similarity(:query, COALESCE(p.description,''))
              ) DESC,
              p.id DESC
            """,
            countQuery = """
            SELECT count(*) FROM products p
            WHERE p.active = true
              AND EXISTS (
                  SELECT 1 FROM product_categories pc
                  WHERE pc.product_id = p.id AND pc.category_id = :categoryId
              )
              AND (
                p.name           ILIKE :pattern
                OR p.description ILIKE :pattern
                OR word_similarity(:query, p.name)                      > 0.3
                OR word_similarity(:query, COALESCE(p.description,''))  > 0.3
              )
            """,
            nativeQuery = true)
    Page<Long> searchActiveByCategoryIds(@Param("query") String query,
                                         @Param("pattern") String pattern,
                                         @Param("categoryId") Long categoryId,
                                         Pageable pageable);

    @Query(value = """
            SELECT p.id FROM products p
            WHERE p.active = true
              AND EXISTS (
                  SELECT 1 FROM product_categories pc
                  WHERE pc.product_id = p.id AND pc.category_id = :categoryId
              )
              AND (
                p.name           ILIKE :pattern
                OR p.description ILIKE :pattern
                OR word_similarity(:query, p.name)                      > 0.3
                OR word_similarity(:query, COALESCE(p.description,''))  > 0.3
              )
            ORDER BY CASE
              WHEN (p.discount_starts_at IS NULL OR p.discount_starts_at <= NOW())
               AND (p.discount_ends_at   IS NULL OR p.discount_ends_at   >  NOW())
               AND p.discount_percent IS NOT NULL AND p.discount_percent > 0
                THEN p.price * (1 - p.discount_percent / 100.0)
              WHEN (p.discount_starts_at IS NULL OR p.discount_starts_at <= NOW())
               AND (p.discount_ends_at   IS NULL OR p.discount_ends_at   >  NOW())
               AND p.discount_amount IS NOT NULL AND p.discount_amount > 0
                THEN GREATEST(0, p.price - p.discount_amount)
              ELSE p.price
            END ASC
            """,
            countQuery = """
            SELECT count(*) FROM products p
            WHERE p.active = true
              AND EXISTS (
                  SELECT 1 FROM product_categories pc
                  WHERE pc.product_id = p.id AND pc.category_id = :categoryId
              )
              AND (
                p.name           ILIKE :pattern
                OR p.description ILIKE :pattern
                OR word_similarity(:query, p.name)                      > 0.3
                OR word_similarity(:query, COALESCE(p.description,''))  > 0.3
              )
            """,
            nativeQuery = true)
    Page<Long> searchActiveByCategoryIdsPriceAsc(@Param("query") String query,
                                                 @Param("pattern") String pattern,
                                                 @Param("categoryId") Long categoryId,
                                                 Pageable pageable);

    @Query(value = """
            SELECT p.id FROM products p
            WHERE p.active = true
              AND EXISTS (
                  SELECT 1 FROM product_categories pc
                  WHERE pc.product_id = p.id AND pc.category_id = :categoryId
              )
              AND (
                p.name           ILIKE :pattern
                OR p.description ILIKE :pattern
                OR word_similarity(:query, p.name)                      > 0.3
                OR word_similarity(:query, COALESCE(p.description,''))  > 0.3
              )
            ORDER BY CASE
              WHEN (p.discount_starts_at IS NULL OR p.discount_starts_at <= NOW())
               AND (p.discount_ends_at   IS NULL OR p.discount_ends_at   >  NOW())
               AND p.discount_percent IS NOT NULL AND p.discount_percent > 0
                THEN p.price * (1 - p.discount_percent / 100.0)
              WHEN (p.discount_starts_at IS NULL OR p.discount_starts_at <= NOW())
               AND (p.discount_ends_at   IS NULL OR p.discount_ends_at   >  NOW())
               AND p.discount_amount IS NOT NULL AND p.discount_amount > 0
                THEN GREATEST(0, p.price - p.discount_amount)
              ELSE p.price
            END DESC
            """,
            countQuery = """
            SELECT count(*) FROM products p
            WHERE p.active = true
              AND EXISTS (
                  SELECT 1 FROM product_categories pc
                  WHERE pc.product_id = p.id AND pc.category_id = :categoryId
              )
              AND (
                p.name           ILIKE :pattern
                OR p.description ILIKE :pattern
                OR word_similarity(:query, p.name)                      > 0.3
                OR word_similarity(:query, COALESCE(p.description,''))  > 0.3
              )
            """,
            nativeQuery = true)
    Page<Long> searchActiveByCategoryIdsPriceDesc(@Param("query") String query,
                                                  @Param("pattern") String pattern,
                                                  @Param("categoryId") Long categoryId,
                                                  Pageable pageable);

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

    @EntityGraph(attributePaths = "categories")
    Optional<Product> findByIdAndActiveTrue(Long id);

    @EntityGraph(attributePaths = "categories")
    Optional<Product> findByIdAndActiveFalse(Long id);

    @EntityGraph(attributePaths = "categories")
    Optional<Product> findWithCategoryById(Long id);

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

    // ── Size-only filter (no category, no search) ─────────────────────────────

    @Query(value = """
            SELECT p.id FROM products p
            WHERE p.active = true
              AND EXISTS (SELECT 1 FROM product_size_stock ps WHERE ps.product_id = p.id AND ps.size = :size AND ps.quantity > 0)
            ORDER BY p.id DESC
            """,
            countQuery = """
            SELECT count(*) FROM products p
            WHERE p.active = true
              AND EXISTS (SELECT 1 FROM product_size_stock ps WHERE ps.product_id = p.id AND ps.size = :size AND ps.quantity > 0)
            """, nativeQuery = true)
    Page<Long> findIdsByActiveAndSize(@Param("size") String size, Pageable pageable);

    @Query(value = """
            SELECT p.id FROM products p
            WHERE p.active = true
              AND EXISTS (SELECT 1 FROM product_size_stock ps WHERE ps.product_id = p.id AND ps.size = :size AND ps.quantity > 0)
            ORDER BY CASE
              WHEN (p.discount_starts_at IS NULL OR p.discount_starts_at <= NOW())
               AND (p.discount_ends_at   IS NULL OR p.discount_ends_at   >  NOW())
               AND p.discount_percent IS NOT NULL AND p.discount_percent > 0
                THEN p.price * (1 - p.discount_percent / 100.0)
              WHEN (p.discount_starts_at IS NULL OR p.discount_starts_at <= NOW())
               AND (p.discount_ends_at   IS NULL OR p.discount_ends_at   >  NOW())
               AND p.discount_amount IS NOT NULL AND p.discount_amount > 0
                THEN GREATEST(0, p.price - p.discount_amount)
              ELSE p.price
            END ASC
            """,
            countQuery = """
            SELECT count(*) FROM products p
            WHERE p.active = true
              AND EXISTS (SELECT 1 FROM product_size_stock ps WHERE ps.product_id = p.id AND ps.size = :size AND ps.quantity > 0)
            """, nativeQuery = true)
    Page<Long> findIdsByActiveAndSizePriceAsc(@Param("size") String size, Pageable pageable);

    @Query(value = """
            SELECT p.id FROM products p
            WHERE p.active = true
              AND EXISTS (SELECT 1 FROM product_size_stock ps WHERE ps.product_id = p.id AND ps.size = :size AND ps.quantity > 0)
            ORDER BY CASE
              WHEN (p.discount_starts_at IS NULL OR p.discount_starts_at <= NOW())
               AND (p.discount_ends_at   IS NULL OR p.discount_ends_at   >  NOW())
               AND p.discount_percent IS NOT NULL AND p.discount_percent > 0
                THEN p.price * (1 - p.discount_percent / 100.0)
              WHEN (p.discount_starts_at IS NULL OR p.discount_starts_at <= NOW())
               AND (p.discount_ends_at   IS NULL OR p.discount_ends_at   >  NOW())
               AND p.discount_amount IS NOT NULL AND p.discount_amount > 0
                THEN GREATEST(0, p.price - p.discount_amount)
              ELSE p.price
            END DESC
            """,
            countQuery = """
            SELECT count(*) FROM products p
            WHERE p.active = true
              AND EXISTS (SELECT 1 FROM product_size_stock ps WHERE ps.product_id = p.id AND ps.size = :size AND ps.quantity > 0)
            """, nativeQuery = true)
    Page<Long> findIdsByActiveAndSizePriceDesc(@Param("size") String size, Pageable pageable);

    // ── Category + size filter ────────────────────────────────────────────────

    @Query(value = """
            SELECT p.id FROM products p
            JOIN product_categories pc ON pc.product_id = p.id
            WHERE p.active = true
              AND pc.category_id = :categoryId
              AND EXISTS (SELECT 1 FROM product_size_stock ps WHERE ps.product_id = p.id AND ps.size = :size AND ps.quantity > 0)
            ORDER BY p.id DESC
            """,
            countQuery = """
            SELECT count(DISTINCT p.id) FROM products p
            JOIN product_categories pc ON pc.product_id = p.id
            WHERE p.active = true
              AND pc.category_id = :categoryId
              AND EXISTS (SELECT 1 FROM product_size_stock ps WHERE ps.product_id = p.id AND ps.size = :size AND ps.quantity > 0)
            """, nativeQuery = true)
    Page<Long> findIdsByCategoryActiveAndSize(@Param("categoryId") Long categoryId,
                                              @Param("size") String size,
                                              Pageable pageable);

    @Query(value = """
            SELECT p.id FROM products p
            JOIN product_categories pc ON pc.product_id = p.id
            WHERE p.active = true
              AND pc.category_id = :categoryId
              AND EXISTS (SELECT 1 FROM product_size_stock ps WHERE ps.product_id = p.id AND ps.size = :size AND ps.quantity > 0)
            ORDER BY CASE
              WHEN (p.discount_starts_at IS NULL OR p.discount_starts_at <= NOW())
               AND (p.discount_ends_at   IS NULL OR p.discount_ends_at   >  NOW())
               AND p.discount_percent IS NOT NULL AND p.discount_percent > 0
                THEN p.price * (1 - p.discount_percent / 100.0)
              WHEN (p.discount_starts_at IS NULL OR p.discount_starts_at <= NOW())
               AND (p.discount_ends_at   IS NULL OR p.discount_ends_at   >  NOW())
               AND p.discount_amount IS NOT NULL AND p.discount_amount > 0
                THEN GREATEST(0, p.price - p.discount_amount)
              ELSE p.price
            END ASC
            """,
            countQuery = """
            SELECT count(DISTINCT p.id) FROM products p
            JOIN product_categories pc ON pc.product_id = p.id
            WHERE p.active = true
              AND pc.category_id = :categoryId
              AND EXISTS (SELECT 1 FROM product_size_stock ps WHERE ps.product_id = p.id AND ps.size = :size AND ps.quantity > 0)
            """, nativeQuery = true)
    Page<Long> findIdsByCategoryActiveAndSizePriceAsc(@Param("categoryId") Long categoryId,
                                                      @Param("size") String size,
                                                      Pageable pageable);

    @Query(value = """
            SELECT p.id FROM products p
            JOIN product_categories pc ON pc.product_id = p.id
            WHERE p.active = true
              AND pc.category_id = :categoryId
              AND EXISTS (SELECT 1 FROM product_size_stock ps WHERE ps.product_id = p.id AND ps.size = :size AND ps.quantity > 0)
            ORDER BY CASE
              WHEN (p.discount_starts_at IS NULL OR p.discount_starts_at <= NOW())
               AND (p.discount_ends_at   IS NULL OR p.discount_ends_at   >  NOW())
               AND p.discount_percent IS NOT NULL AND p.discount_percent > 0
                THEN p.price * (1 - p.discount_percent / 100.0)
              WHEN (p.discount_starts_at IS NULL OR p.discount_starts_at <= NOW())
               AND (p.discount_ends_at   IS NULL OR p.discount_ends_at   >  NOW())
               AND p.discount_amount IS NOT NULL AND p.discount_amount > 0
                THEN GREATEST(0, p.price - p.discount_amount)
              ELSE p.price
            END DESC
            """,
            countQuery = """
            SELECT count(DISTINCT p.id) FROM products p
            JOIN product_categories pc ON pc.product_id = p.id
            WHERE p.active = true
              AND pc.category_id = :categoryId
              AND EXISTS (SELECT 1 FROM product_size_stock ps WHERE ps.product_id = p.id AND ps.size = :size AND ps.quantity > 0)
            """, nativeQuery = true)
    Page<Long> findIdsByCategoryActiveAndSizePriceDesc(@Param("categoryId") Long categoryId,
                                                       @Param("size") String size,
                                                       Pageable pageable);

    // ── Search + size (no category) ───────────────────────────────────────────

    @Query(value = """
            SELECT p.id FROM products p
            WHERE p.active = true
              AND EXISTS (SELECT 1 FROM product_size_stock ps WHERE ps.product_id = p.id AND ps.size = :size AND ps.quantity > 0)
              AND (p.name ILIKE :pattern OR p.description ILIKE :pattern
                OR word_similarity(:query, p.name) > 0.3
                OR word_similarity(:query, COALESCE(p.description,'')) > 0.3)
            ORDER BY GREATEST(word_similarity(:query, p.name),
                              word_similarity(:query, COALESCE(p.description,''))) DESC, p.id DESC
            """,
            countQuery = """
            SELECT count(*) FROM products p
            WHERE p.active = true
              AND EXISTS (SELECT 1 FROM product_size_stock ps WHERE ps.product_id = p.id AND ps.size = :size AND ps.quantity > 0)
              AND (p.name ILIKE :pattern OR p.description ILIKE :pattern
                OR word_similarity(:query, p.name) > 0.3
                OR word_similarity(:query, COALESCE(p.description,'')) > 0.3)
            """, nativeQuery = true)
    Page<Long> searchActiveIdsAndSize(@Param("query") String query,
                                      @Param("pattern") String pattern,
                                      @Param("size") String size,
                                      Pageable pageable);

    @Query(value = """
            SELECT p.id FROM products p
            WHERE p.active = true
              AND EXISTS (SELECT 1 FROM product_size_stock ps WHERE ps.product_id = p.id AND ps.size = :size AND ps.quantity > 0)
              AND (p.name ILIKE :pattern OR p.description ILIKE :pattern
                OR word_similarity(:query, p.name) > 0.3
                OR word_similarity(:query, COALESCE(p.description,'')) > 0.3)
            ORDER BY CASE
              WHEN (p.discount_starts_at IS NULL OR p.discount_starts_at <= NOW())
               AND (p.discount_ends_at IS NULL OR p.discount_ends_at > NOW())
               AND p.discount_percent IS NOT NULL AND p.discount_percent > 0
                THEN p.price * (1 - p.discount_percent / 100.0)
              WHEN (p.discount_starts_at IS NULL OR p.discount_starts_at <= NOW())
               AND (p.discount_ends_at IS NULL OR p.discount_ends_at > NOW())
               AND p.discount_amount IS NOT NULL AND p.discount_amount > 0
                THEN GREATEST(0, p.price - p.discount_amount)
              ELSE p.price END ASC
            """,
            countQuery = """
            SELECT count(*) FROM products p
            WHERE p.active = true
              AND EXISTS (SELECT 1 FROM product_size_stock ps WHERE ps.product_id = p.id AND ps.size = :size AND ps.quantity > 0)
              AND (p.name ILIKE :pattern OR p.description ILIKE :pattern
                OR word_similarity(:query, p.name) > 0.3
                OR word_similarity(:query, COALESCE(p.description,'')) > 0.3)
            """, nativeQuery = true)
    Page<Long> searchActiveIdsAndSizePriceAsc(@Param("query") String query,
                                              @Param("pattern") String pattern,
                                              @Param("size") String size,
                                              Pageable pageable);

    @Query(value = """
            SELECT p.id FROM products p
            WHERE p.active = true
              AND EXISTS (SELECT 1 FROM product_size_stock ps WHERE ps.product_id = p.id AND ps.size = :size AND ps.quantity > 0)
              AND (p.name ILIKE :pattern OR p.description ILIKE :pattern
                OR word_similarity(:query, p.name) > 0.3
                OR word_similarity(:query, COALESCE(p.description,'')) > 0.3)
            ORDER BY CASE
              WHEN (p.discount_starts_at IS NULL OR p.discount_starts_at <= NOW())
               AND (p.discount_ends_at IS NULL OR p.discount_ends_at > NOW())
               AND p.discount_percent IS NOT NULL AND p.discount_percent > 0
                THEN p.price * (1 - p.discount_percent / 100.0)
              WHEN (p.discount_starts_at IS NULL OR p.discount_starts_at <= NOW())
               AND (p.discount_ends_at IS NULL OR p.discount_ends_at > NOW())
               AND p.discount_amount IS NOT NULL AND p.discount_amount > 0
                THEN GREATEST(0, p.price - p.discount_amount)
              ELSE p.price END DESC
            """,
            countQuery = """
            SELECT count(*) FROM products p
            WHERE p.active = true
              AND EXISTS (SELECT 1 FROM product_size_stock ps WHERE ps.product_id = p.id AND ps.size = :size AND ps.quantity > 0)
              AND (p.name ILIKE :pattern OR p.description ILIKE :pattern
                OR word_similarity(:query, p.name) > 0.3
                OR word_similarity(:query, COALESCE(p.description,'')) > 0.3)
            """, nativeQuery = true)
    Page<Long> searchActiveIdsAndSizePriceDesc(@Param("query") String query,
                                               @Param("pattern") String pattern,
                                               @Param("size") String size,
                                               Pageable pageable);

    // ── Search + category + size ──────────────────────────────────────────────

    @Query(value = """
            SELECT p.id FROM products p
            WHERE p.active = true
              AND EXISTS (SELECT 1 FROM product_categories pc WHERE pc.product_id = p.id AND pc.category_id = :categoryId)
              AND EXISTS (SELECT 1 FROM product_size_stock ps WHERE ps.product_id = p.id AND ps.size = :size AND ps.quantity > 0)
              AND (p.name ILIKE :pattern OR p.description ILIKE :pattern
                OR word_similarity(:query, p.name) > 0.3
                OR word_similarity(:query, COALESCE(p.description,'')) > 0.3)
            ORDER BY GREATEST(word_similarity(:query, p.name),
                              word_similarity(:query, COALESCE(p.description,''))) DESC, p.id DESC
            """,
            countQuery = """
            SELECT count(*) FROM products p
            WHERE p.active = true
              AND EXISTS (SELECT 1 FROM product_categories pc WHERE pc.product_id = p.id AND pc.category_id = :categoryId)
              AND EXISTS (SELECT 1 FROM product_size_stock ps WHERE ps.product_id = p.id AND ps.size = :size AND ps.quantity > 0)
              AND (p.name ILIKE :pattern OR p.description ILIKE :pattern
                OR word_similarity(:query, p.name) > 0.3
                OR word_similarity(:query, COALESCE(p.description,'')) > 0.3)
            """, nativeQuery = true)
    Page<Long> searchActiveByCategoryIdsAndSize(@Param("query") String query,
                                                @Param("pattern") String pattern,
                                                @Param("categoryId") Long categoryId,
                                                @Param("size") String size,
                                                Pageable pageable);

    @Query(value = """
            SELECT p.id FROM products p
            WHERE p.active = true
              AND EXISTS (SELECT 1 FROM product_categories pc WHERE pc.product_id = p.id AND pc.category_id = :categoryId)
              AND EXISTS (SELECT 1 FROM product_size_stock ps WHERE ps.product_id = p.id AND ps.size = :size AND ps.quantity > 0)
              AND (p.name ILIKE :pattern OR p.description ILIKE :pattern
                OR word_similarity(:query, p.name) > 0.3
                OR word_similarity(:query, COALESCE(p.description,'')) > 0.3)
            ORDER BY CASE
              WHEN (p.discount_starts_at IS NULL OR p.discount_starts_at <= NOW())
               AND (p.discount_ends_at IS NULL OR p.discount_ends_at > NOW())
               AND p.discount_percent IS NOT NULL AND p.discount_percent > 0
                THEN p.price * (1 - p.discount_percent / 100.0)
              WHEN (p.discount_starts_at IS NULL OR p.discount_starts_at <= NOW())
               AND (p.discount_ends_at IS NULL OR p.discount_ends_at > NOW())
               AND p.discount_amount IS NOT NULL AND p.discount_amount > 0
                THEN GREATEST(0, p.price - p.discount_amount)
              ELSE p.price END ASC
            """,
            countQuery = """
            SELECT count(*) FROM products p
            WHERE p.active = true
              AND EXISTS (SELECT 1 FROM product_categories pc WHERE pc.product_id = p.id AND pc.category_id = :categoryId)
              AND EXISTS (SELECT 1 FROM product_size_stock ps WHERE ps.product_id = p.id AND ps.size = :size AND ps.quantity > 0)
              AND (p.name ILIKE :pattern OR p.description ILIKE :pattern
                OR word_similarity(:query, p.name) > 0.3
                OR word_similarity(:query, COALESCE(p.description,'')) > 0.3)
            """, nativeQuery = true)
    Page<Long> searchActiveByCategoryIdsAndSizePriceAsc(@Param("query") String query,
                                                        @Param("pattern") String pattern,
                                                        @Param("categoryId") Long categoryId,
                                                        @Param("size") String size,
                                                        Pageable pageable);

    @Query(value = """
            SELECT p.id FROM products p
            WHERE p.active = true
              AND EXISTS (SELECT 1 FROM product_categories pc WHERE pc.product_id = p.id AND pc.category_id = :categoryId)
              AND EXISTS (SELECT 1 FROM product_size_stock ps WHERE ps.product_id = p.id AND ps.size = :size AND ps.quantity > 0)
              AND (p.name ILIKE :pattern OR p.description ILIKE :pattern
                OR word_similarity(:query, p.name) > 0.3
                OR word_similarity(:query, COALESCE(p.description,'')) > 0.3)
            ORDER BY CASE
              WHEN (p.discount_starts_at IS NULL OR p.discount_starts_at <= NOW())
               AND (p.discount_ends_at IS NULL OR p.discount_ends_at > NOW())
               AND p.discount_percent IS NOT NULL AND p.discount_percent > 0
                THEN p.price * (1 - p.discount_percent / 100.0)
              WHEN (p.discount_starts_at IS NULL OR p.discount_starts_at <= NOW())
               AND (p.discount_ends_at IS NULL OR p.discount_ends_at > NOW())
               AND p.discount_amount IS NOT NULL AND p.discount_amount > 0
                THEN GREATEST(0, p.price - p.discount_amount)
              ELSE p.price END DESC
            """,
            countQuery = """
            SELECT count(*) FROM products p
            WHERE p.active = true
              AND EXISTS (SELECT 1 FROM product_categories pc WHERE pc.product_id = p.id AND pc.category_id = :categoryId)
              AND EXISTS (SELECT 1 FROM product_size_stock ps WHERE ps.product_id = p.id AND ps.size = :size AND ps.quantity > 0)
              AND (p.name ILIKE :pattern OR p.description ILIKE :pattern
                OR word_similarity(:query, p.name) > 0.3
                OR word_similarity(:query, COALESCE(p.description,'')) > 0.3)
            """, nativeQuery = true)
    Page<Long> searchActiveByCategoryIdsAndSizePriceDesc(@Param("query") String query,
                                                         @Param("pattern") String pattern,
                                                         @Param("categoryId") Long categoryId,
                                                         @Param("size") String size,
                                                         Pageable pageable);
}
