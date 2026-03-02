package com.portfolio.silver_lady_s.repository;

import com.portfolio.silver_lady_s.entity.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = "category")
    List<Product> findAllByActiveTrueOrderByIdDesc();

    @EntityGraph(attributePaths = "category")
    Optional<Product> findByIdAndActiveTrue(Long id);

    @EntityGraph(attributePaths = "category")
    List<Product> findByCategoryIdAndActiveTrueOrderByIdDesc(Long categoryId);

    @EntityGraph(attributePaths = "category")
    List<Product> findByNameContainingIgnoreCaseAndActiveTrueOrderByIdDesc(String name);

    @EntityGraph(attributePaths = "category")
    List<Product> findByCategoryIdAndNameContainingIgnoreCaseAndActiveTrueOrderByIdDesc(Long categoryId, String name);

    @EntityGraph(attributePaths = "category")
    Optional<Product> findWithCategoryById(Long id);
}
