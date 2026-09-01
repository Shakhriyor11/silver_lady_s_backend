CREATE TABLE site_settings (
    id                     BIGSERIAL PRIMARY KEY,
    singleton_key          INTEGER NOT NULL DEFAULT 1 UNIQUE CHECK (singleton_key = 1),
    leaves_effect_enabled  BOOLEAN NOT NULL DEFAULT FALSE,
    created_at             TIMESTAMP,
    updated_at             TIMESTAMP
);
