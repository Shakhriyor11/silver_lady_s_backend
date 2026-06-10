CREATE TABLE promo_banners (
    id            BIGSERIAL PRIMARY KEY,
    image_url     VARCHAR(500),
    title_uz      VARCHAR(200),
    title_ru      VARCHAR(200),
    title_en      VARCHAR(200),
    subtitle_uz   VARCHAR(400),
    subtitle_ru   VARCHAR(400),
    subtitle_en   VARCHAR(400),
    display_order INTEGER NOT NULL DEFAULT 0,
    active        BOOLEAN NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_promo_banner_order ON promo_banners (display_order);
