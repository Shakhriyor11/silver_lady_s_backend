ALTER TABLE products ADD COLUMN IF NOT EXISTS barcode VARCHAR(64);

CREATE UNIQUE INDEX IF NOT EXISTS uk_products_barcode ON products (barcode);
