package com.portfolio.silver_lady_s.repository;

import com.portfolio.silver_lady_s.entity.AboutUs;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AboutUsRepository extends JpaRepository<AboutUs, Long> {
    Optional<AboutUs> findTopByOrderByIdAsc();
}
