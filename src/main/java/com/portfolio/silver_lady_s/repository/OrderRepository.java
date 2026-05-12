package com.portfolio.silver_lady_s.repository;

import com.portfolio.silver_lady_s.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"items"})
    Optional<Order> findByIdAndUserId(Long id, Long userId);

    // Admin: barcha buyurtmalar (user ham yuklanadi)
    @EntityGraph(attributePaths = {"user"})
    Page<Order> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "items"})
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findByIdWithDetails(@Param("id") Long id);
}
