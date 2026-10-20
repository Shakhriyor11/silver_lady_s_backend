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
        // ── Missing columns (DDL auto can't add NOT NULL without DEFAULT) ────────
        runSafe("ALTER TABLE users ADD COLUMN IF NOT EXISTS phone_verified BOOLEAN NOT NULL DEFAULT FALSE");
        runSafe("ALTER TABLE users ADD COLUMN IF NOT EXISTS otp VARCHAR(6)");
        runSafe("ALTER TABLE users ADD COLUMN IF NOT EXISTS otp_expires_at TIMESTAMP WITH TIME ZONE");

        runSafe("ALTER TABLE about_us ADD COLUMN IF NOT EXISTS created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT NOW()");
        runSafe("ALTER TABLE about_us ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT NOW()");

        // ── SEO: slug ustunlari + backfill + unique index (ddl-auto buni to'g'ri qila olmaydi) ──
        runSafe("ALTER TABLE products ADD COLUMN IF NOT EXISTS slug VARCHAR(220)");
        runSafe("ALTER TABLE categories ADD COLUMN IF NOT EXISTS slug VARCHAR(120)");
        runSafe("""
                UPDATE products
                SET slug = lower(regexp_replace(regexp_replace(trim(name), '[^a-zA-Z0-9]+', '-', 'g'), '(^-+|-+$)', '', 'g')) || '-' || id
                WHERE slug IS NULL
                """);
        runSafe("""
                UPDATE categories
                SET slug = lower(regexp_replace(regexp_replace(trim(name), '[^a-zA-Z0-9]+', '-', 'g'), '(^-+|-+$)', '', 'g')) || '-' || id
                WHERE slug IS NULL
                """);
        runSafe("CREATE UNIQUE INDEX IF NOT EXISTS idx_products_slug ON products(slug)");
        runSafe("CREATE UNIQUE INDEX IF NOT EXISTS idx_categories_slug ON categories(slug)");

        // ── pg_trgm full-text search indexes ─────────────────────────────────────
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

    private void runSafe(String sql) {
        try {
            jdbc.execute(sql);
        } catch (Exception e) {
            log.warn("Schema migration skipped: {} — {}", sql.split(" ")[5], e.getMessage());
        }
    }
}
