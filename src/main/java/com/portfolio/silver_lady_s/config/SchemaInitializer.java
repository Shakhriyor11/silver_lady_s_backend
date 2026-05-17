package com.portfolio.silver_lady_s.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Ilovani ishga tushirishda bir marta ishlaydi.
 * pg_trgm extension va GIN indekslari idempotent ravishda yaratiladi.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SchemaInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbc;

    @Override
    public void run(ApplicationArguments args) {
        try {
            jdbc.execute("CREATE EXTENSION IF NOT EXISTS pg_trgm");

            jdbc.execute("""
                    CREATE INDEX IF NOT EXISTS idx_product_name_trgm
                    ON products USING GIN (name gin_trgm_ops)
                    """);

            jdbc.execute("""
                    CREATE INDEX IF NOT EXISTS idx_product_desc_trgm
                    ON products USING GIN (description gin_trgm_ops)
                    """);

            log.info("pg_trgm extension and search indexes are ready");
        } catch (Exception e) {
            // Test muhitida yoki pg_trgm mavjud bo'lmasa ilovani to'xtatmasin
            log.warn("Could not initialize pg_trgm indexes: {}", e.getMessage());
        }
    }
}
