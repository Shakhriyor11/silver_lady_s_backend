-- Mahsulot o'lchamlari jadvali
CREATE TABLE IF NOT EXISTS product_sizes (
    product_id  BIGINT      NOT NULL,
    size        VARCHAR(30) NOT NULL,
    size_order  INT         NOT NULL,
    CONSTRAINT fk_product_size_product
        FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE
);

-- Savatcha elementi tanlangan o'lcham
ALTER TABLE cart_items
    ADD COLUMN IF NOT EXISTS selected_size VARCHAR(30);

-- Savatcha elementi unique constraint olib tashlash (o'lcham bilan farqlanadi)
ALTER TABLE cart_items
    DROP CONSTRAINT IF EXISTS uk_cart_product;

-- Buyurtma elementi tanlangan o'lcham (snapshot)
ALTER TABLE order_items
    ADD COLUMN IF NOT EXISTS selected_size VARCHAR(30);

-- Mahsulot ombordagi miqdor
ALTER TABLE products
    ADD COLUMN IF NOT EXISTS stock_quantity INT NOT NULL DEFAULT 0;
