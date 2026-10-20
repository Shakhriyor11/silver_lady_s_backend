package com.portfolio.silver_lady_s.repository;

import java.time.Instant;

/** sitemap.xml generatsiyasi uchun yengil proyeksiya */
public interface SlugProjection {
    Long getId();
    String getSlug();
    Instant getUpdatedAt();
}
