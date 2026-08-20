package com.portfolio.silver_lady_s.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ProductFilterRepositoryImpl implements ProductFilterRepository {

    @PersistenceContext
    private EntityManager em;

    private static final String EFFECTIVE_PRICE = """
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
            END""";

    @Override
    public Page<Long> findActiveIds(String search, Long categoryId, String size, String sort,
                                     boolean includeArchived, Pageable pageable) {
        StringBuilder where = new StringBuilder(includeArchived ? "WHERE 1=1\n" : "WHERE p.active = true\n");
        Map<String, Object> params = new LinkedHashMap<>();

        if (categoryId != null) {
            where.append("""
                      AND EXISTS (
                        SELECT 1 FROM product_categories pc
                        WHERE pc.product_id = p.id
                          AND pc.category_id IN (
                            SELECT c.id FROM categories c
                            WHERE c.id = :categoryId OR c.parent_id = :categoryId
                          )
                      )
                    """);
            params.put("categoryId", categoryId);
        }
        if (size != null) {
            where.append("  AND EXISTS (SELECT 1 FROM product_size_stock ps WHERE ps.product_id = p.id AND ps.size = :size AND ps.quantity > 0)\n");
            params.put("size", size);
        }
        if (search != null) {
            where.append("""
                      AND (p.name ILIKE :pattern OR p.description ILIKE :pattern
                        OR word_similarity(:search, p.name) > 0.3
                        OR word_similarity(:search, COALESCE(p.description, '')) > 0.3)
                    """);
            params.put("search", search);
            params.put("pattern", "%" + search + "%");
        }

        String orderBy = resolveOrderBy(sort, search);
        String dataSql  = "SELECT p.id FROM products p\n" + where + orderBy;
        String countSql = "SELECT count(DISTINCT p.id) FROM products p\n" + where;

        var dataQuery  = em.createNativeQuery(dataSql);
        var countQuery = em.createNativeQuery(countSql);
        params.forEach(dataQuery::setParameter);
        params.forEach(countQuery::setParameter);

        dataQuery.setFirstResult((int) pageable.getOffset());
        dataQuery.setMaxResults(pageable.getPageSize());

        @SuppressWarnings("unchecked")
        List<Number> raw = dataQuery.getResultList();
        List<Long> ids = raw.stream().map(Number::longValue).toList();
        long total = ((Number) countQuery.getSingleResult()).longValue();

        return new PageImpl<>(ids, pageable, total);
    }

    private static String resolveOrderBy(String sort, String search) {
        if ("price_asc".equals(sort))  return "ORDER BY " + EFFECTIVE_PRICE + " ASC, p.id DESC\n";
        if ("price_desc".equals(sort)) return "ORDER BY " + EFFECTIVE_PRICE + " DESC, p.id DESC\n";
        if (search != null) {
            return """
                    ORDER BY GREATEST(
                      word_similarity(:search, p.name),
                      word_similarity(:search, COALESCE(p.description, ''))
                    ) DESC, p.id DESC
                    """;
        }
        return "ORDER BY p.id DESC\n";
    }
}
