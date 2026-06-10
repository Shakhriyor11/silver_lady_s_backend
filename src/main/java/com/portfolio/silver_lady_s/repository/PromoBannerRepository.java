package com.portfolio.silver_lady_s.repository;

import com.portfolio.silver_lady_s.entity.PromoBanner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PromoBannerRepository extends JpaRepository<PromoBanner, Long> {

    List<PromoBanner> findByActiveTrueOrderByDisplayOrderAscIdAsc();

    List<PromoBanner> findAllByOrderByDisplayOrderAscIdAsc();
}
