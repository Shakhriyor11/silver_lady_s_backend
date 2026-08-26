CREATE TABLE IF NOT EXISTS wishlist_items (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_id  BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    created_at  timestamp(6) with time zone NOT NULL DEFAULT now(),
    updated_at  timestamp(6) with time zone NOT NULL DEFAULT now(),
    CONSTRAINT uk_wishlist_user_product UNIQUE (user_id, product_id)
);

CREATE INDEX IF NOT EXISTS idx_wishlist_user_id ON wishlist_items(user_id);
