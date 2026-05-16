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
    public static final String CACHE_CAROUSEL   = "carousel";

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(
                build(CACHE_CATEGORIES, 10, TimeUnit.MINUTES, 100),
                build(CACHE_ABOUT,       30, TimeUnit.MINUTES,  10),
                build(CACHE_CAROUSEL,     5, TimeUnit.MINUTES,  10)
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
