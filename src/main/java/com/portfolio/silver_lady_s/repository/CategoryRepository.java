package com.portfolio.silver_lady_s.repository;

import com.portfolio.silver_lady_s.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByNameIgnoreCase(String name);
    Optional<Category> findByNameIgnoreCase(String name);
    List<Category> findAllByOrderByIdAsc();
}
