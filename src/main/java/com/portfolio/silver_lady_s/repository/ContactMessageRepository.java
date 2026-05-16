package com.portfolio.silver_lady_s.repository;

import com.portfolio.silver_lady_s.entity.ContactMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {

    @Override
    @EntityGraph(attributePaths = {"user", "product", "product.categories"})
    @Query(value = "SELECT m FROM ContactMessage m",
           countQuery = "SELECT count(m) FROM ContactMessage m")
    Page<ContactMessage> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "product", "product.categories"})
    @Query("SELECT m FROM ContactMessage m WHERE m.id = :id")
    Optional<ContactMessage> findByIdWithDetails(@Param("id") Long id);

    @EntityGraph(attributePaths = {"product", "product.categories"})
    @Query("SELECT m FROM ContactMessage m WHERE m.user.id = :userId ORDER BY m.createdAt DESC")
    Page<ContactMessage> findByUserId(@Param("userId") Long userId, Pageable pageable);
}
