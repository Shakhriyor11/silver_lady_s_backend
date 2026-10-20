package com.portfolio.silver_lady_s.repository;

import com.portfolio.silver_lady_s.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Root kategoriyalar (ota kategoriya yo'q), sort_order bo'yicha tartibda
    List<Category> findAllByParentIsNullOrderBySortOrderAscIdAsc();

    // Ism tekshiruvi: root darajada
    boolean existsByNameIgnoreCaseAndParentIsNull(String name);
    Optional<Category> findByNameIgnoreCaseAndParentIsNull(String name);

    // Ism tekshiruvi: ma'lum parent ostida
    boolean existsByNameIgnoreCaseAndParentId(String name, Long parentId);
    Optional<Category> findByNameIgnoreCaseAndParentId(String name, Long parentId);

    // ── Sitemap: faqat yengil ustunlar (id, slug, updatedAt) ─────────────────
    @Query("SELECT c.id AS id, c.slug AS slug, c.updatedAt AS updatedAt FROM Category c")
    List<SlugProjection> findAllSlugsForSitemap();
}
