package com.portfolio.silver_lady_s.repository;

import com.portfolio.silver_lady_s.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"user", "items"})
    Page<Order> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "items"})
    Optional<Order> findByIdAndUserId(Long id, Long userId);

    // Admin: 2-query pattern to avoid pagination-in-memory with collection JOIN FETCH
    @Query("SELECT o.id FROM Order o")
    Page<Long> findAllIds(Pageable pageable);

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.user LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.product WHERE o.id IN :ids")
    List<Order> findAllWithItemsByIds(@Param("ids") List<Long> ids);

    @EntityGraph(attributePaths = {"user", "items"})
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :from")
    Long countOrdersSince(@Param("from") Instant from);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.createdAt >= :from")
    BigDecimal sumRevenueSince(@Param("from") Instant from);

    @Query("SELECT SUM(o.totalAmount) FROM Order o")
    BigDecimal sumTotalRevenue();

    @Query(value = """
            SELECT DATE(created_at AT TIME ZONE 'UTC')::text AS date,
                   COUNT(*) AS orders,
                   COALESCE(SUM(total_amount), 0) AS revenue
            FROM orders
            WHERE created_at >= :from
            GROUP BY DATE(created_at AT TIME ZONE 'UTC')
            ORDER BY date
            """, nativeQuery = true)
    List<Object[]> findDailyOrderStats(@Param("from") Instant from);

    @Query(value = """
            SELECT status, COUNT(*) FROM orders GROUP BY status
            """, nativeQuery = true)
    List<Object[]> findOrderStatusCounts();
}
