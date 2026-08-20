package com.portfolio.silver_lady_s.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductFilterRepository {
    Page<Long> findActiveIds(String search, Long categoryId, String size, String sort, boolean includeArchived, Pageable pageable);
}
