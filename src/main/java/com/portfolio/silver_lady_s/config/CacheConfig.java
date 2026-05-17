package com.portfolio.silver_lady_s.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    public static final String CACHE_CATEGORIES = "categories";
    public static final String CACHE_ABOUT      = "about";

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(
                // Kategoriyalar kamdan-kam o'zgaradi — 10 daqiqa
                build(CACHE_CATEGORIES, 10, TimeUnit.MINUTES, 100),
                // About sahifasi deyarli o'zgarmaydi — 30 daqiqa
                build(CACHE_ABOUT, 30, TimeUnit.MINUTES, 10)
        ));
        return manager;
    }

    private CaffeineCache build(String name, long duration, TimeUnit unit, long maxSize) {
        return new CaffeineCache(name,
                Caffeine.newBuilder()
                        .expireAfterWrite(duration, unit)
                        .maximumSize(maxSize)
                        .recordStats()
                        .build());
    }
}
