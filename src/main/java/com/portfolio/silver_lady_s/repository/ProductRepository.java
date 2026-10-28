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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductFilterRepository {

    // ── Simple list views (no filter) — id-only projection ───────────────────
    // Qasddan Page<Long>: Page<Product> + @EntityGraph(categories) Pageable bilan
    // ishlatilsa, categories @ManyToMany JOIN tufayli Hibernate SQL darajasida
    // LIMIT/OFFSET qila olmay, BUTUN jadvalni xotiraga yuklab, sahifalashni
    // Java'da qo'lda bajaradi (HHH90003004: "applying in memory"). Bu yerda
    // avval faqat id lar sahifalanadi (haqiqiy SQL LIMIT/OFFSET bilan), keyin
    // fetchByIds() faqat o'sha sahifadagi qatorlar uchun JOIN FETCH qiladi.

    @Query("SELECT p.id FROM Product p WHERE p.active = true ORDER BY p.id DESC")
    Page<Long> findIdsByActiveTrueOrderByIdDesc(Pageable pageable);

    @Query("SELECT p.id FROM Product p WHERE p.active = true ORDER BY p.price ASC")
    Page<Long> findIdsByActiveTrueOrderByPriceAsc(Pageable pageable);

    @Query("SELECT p.id FROM Product p WHERE p.active = true ORDER BY p.price DESC")
    Page<Long> findIdsByActiveTrueOrderByPriceDesc(Pageable pageable);

    // ── Arxiv: arxivga tushgan sana yoki saytga yuklangan sana bo'yicha ──────
    // archivedAt null bo'lsa (ustun qo'shilishidan oldin arxivlanganlar) updatedAt olinadi

    @Query("SELECT p.id FROM Product p WHERE p.active = false ORDER BY COALESCE(p.archivedAt, p.updatedAt) DESC, p.id DESC")
    Page<Long> findArchivedIdsOrderByArchivedAtDesc(Pageable pageable);

    @Query("SELECT p.id FROM Product p WHERE p.active = false ORDER BY COALESCE(p.archivedAt, p.updatedAt) ASC, p.id ASC")
    Page<Long> findArchivedIdsOrderByArchivedAtAsc(Pageable pageable);

    @Query("SELECT p.id FROM Product p WHERE p.active = false ORDER BY p.createdAt DESC, p.id DESC")
    Page<Long> findArchivedIdsOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT p.id FROM Product p WHERE p.active = false ORDER BY p.createdAt ASC, p.id ASC")
    Page<Long> findArchivedIdsOrderByCreatedAtAsc(Pageable pageable);

    // ── Admin view: active + archived together ────────────────────────────────

    @Query("SELECT p.id FROM Product p ORDER BY p.id DESC")
    Page<Long> findIdsByOrderByIdDesc(Pageable pageable);

    @Query("SELECT p.id FROM Product p ORDER BY p.price ASC")
    Page<Long> findIdsByOrderByPriceAsc(Pageable pageable);

    @Query("SELECT p.id FROM Product p ORDER BY p.price DESC")
    Page<Long> findIdsByOrderByPriceDesc(Pageable pageable);

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

    @EntityGraph(attributePaths = "categories")
    List<Product> findAllByCategoriesId(Long categoryId);

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

    // ── Home page: top-N products per category in a single round trip ────────

    @Query(value = """
            SELECT sub.category_id, sub.product_id
            FROM (
                SELECT pc.category_id AS category_id, p.id AS product_id,
                       ROW_NUMBER() OVER (PARTITION BY pc.category_id ORDER BY p.id DESC) AS rn
                FROM products p
                JOIN product_categories pc ON pc.product_id = p.id
                WHERE p.active = true AND pc.category_id IN (:categoryIds)
            ) sub
            WHERE sub.rn <= :limit
            ORDER BY sub.category_id, sub.rn
            """, nativeQuery = true)
    List<Object[]> findTopProductIdsByCategoryIds(@Param("categoryIds") List<Long> categoryIds,
                                                   @Param("limit") int limit);

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

    // ── Admin statistika: saytdagi (faol) tovarlarning umumiy summasi ────────
    // Joriy (chegirma hisobga olingan) narx × qoldiq. Har so'rovda NOW() bo'yicha
    // hisoblanadi, shuning uchun chegirma boshlanishi/tugashi, tovar qo'shilishi,
    // sotilishi yoki arxivlanishi darhol aks etadi. Formula ProductFilterRepositoryImpl
    // dagi EFFECTIVE_PRICE va ProductDto#computeSalePrice bilan bir xil.
    @Query(value = """
            SELECT COALESCE(SUM(ROUND(
              CASE
                WHEN (p.discount_starts_at IS NULL OR p.discount_starts_at <= NOW())
                 AND (p.discount_ends_at   IS NULL OR p.discount_ends_at   >  NOW())
                 AND p.discount_percent IS NOT NULL AND p.discount_percent > 0
                  THEN p.price * (1 - p.discount_percent / 100.0)
                WHEN (p.discount_starts_at IS NULL OR p.discount_starts_at <= NOW())
                 AND (p.discount_ends_at   IS NULL OR p.discount_ends_at   >  NOW())
                 AND p.discount_amount IS NOT NULL AND p.discount_amount > 0
                  THEN GREATEST(0, p.price - p.discount_amount)
                ELSE p.price
              END, 2) * p.stock_quantity), 0)
            FROM products p
            WHERE p.active = true
            """, nativeQuery = true)
    BigDecimal sumActiveStockValue();

    @Query("SELECT COALESCE(SUM(p.price * p.stockQuantity), 0) FROM Product p WHERE p.active = true")
    BigDecimal sumActiveStockValueWithoutDiscount();

    @Query("SELECT COALESCE(SUM(p.stockQuantity), 0) FROM Product p WHERE p.active = true")
    Long sumActiveStockQuantity();

    long countByActiveTrue();

    // ── Sitemap: faqat yengil ustunlar (id, slug, updatedAt) ─────────────────
    @Query("SELECT p.id AS id, p.slug AS slug, p.updatedAt AS updatedAt FROM Product p WHERE p.active = true AND p.slug IS NOT NULL")
    List<SlugProjection> findActiveSlugsForSitemap();
}
