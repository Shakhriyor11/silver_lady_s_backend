package com.portfolio.silver_lady_s.repository;

import com.portfolio.silver_lady_s.entity.SiteSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SiteSettingsRepository extends JpaRepository<SiteSettings, Long> {
    Optional<SiteSettings> findTopByOrderByIdAsc();
}
