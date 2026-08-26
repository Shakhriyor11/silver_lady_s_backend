package com.portfolio.silver_lady_s.repository;

import com.portfolio.silver_lady_s.entity.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    @EntityGraph(attributePaths = {"cashier", "items"})
    Optional<Sale> findWithDetailsById(Long id);

    // Admin: 2-query pattern to avoid pagination-in-memory with collection JOIN FETCH
    @Query("""
            SELECT s.id FROM Sale s
            WHERE (:cashierId IS NULL OR s.cashier.id = :cashierId)
              AND (:from IS NULL OR s.createdAt >= :from)
              AND (:to IS NULL OR s.createdAt < :to)
            ORDER BY s.createdAt DESC
            """)
    Page<Long> findFilteredIds(@Param("cashierId") Long cashierId,
                                @Param("from") Instant from,
                                @Param("to") Instant to,
                                Pageable pageable);

    @Query("""
            SELECT DISTINCT s FROM Sale s
            LEFT JOIN FETCH s.cashier
            LEFT JOIN FETCH s.items i
            LEFT JOIN FETCH i.product
            WHERE s.id IN :ids
            """)
    List<Sale> findAllWithItemsByIds(@Param("ids") List<Long> ids);
}
