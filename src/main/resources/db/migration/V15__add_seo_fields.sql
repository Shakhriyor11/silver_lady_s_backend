ALTER TABLE products ADD COLUMN IF NOT EXISTS slug VARCHAR(220);
ALTER TABLE products ADD COLUMN IF NOT EXISTS seo_title VARCHAR(160);
ALTER TABLE products ADD COLUMN IF NOT EXISTS seo_description VARCHAR(300);

ALTER TABLE categories ADD COLUMN IF NOT EXISTS slug VARCHAR(120);
ALTER TABLE categories ADD COLUMN IF NOT EXISTS seo_title VARCHAR(160);
ALTER TABLE categories ADD COLUMN IF NOT EXISTS seo_description VARCHAR(300);

ALTER TABLE product_images ADD COLUMN IF NOT EXISTS alt_text VARCHAR(255);

-- Mavjud yozuvlar uchun slug backfill: nomdan + id dan (to'qnashuv imkonsiz)
UPDATE products
SET slug = lower(regexp_replace(regexp_replace(trim(name), '[^a-zA-Z0-9]+', '-', 'g'), '(^-+|-+$)', '', 'g')) || '-' || id
WHERE slug IS NULL;

UPDATE categories
SET slug = lower(regexp_replace(regexp_replace(trim(name), '[^a-zA-Z0-9]+', '-', 'g'), '(^-+|-+$)', '', 'g')) || '-' || id
WHERE slug IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS idx_products_slug ON products(slug);
CREATE UNIQUE INDEX IF NOT EXISTS idx_categories_slug ON categories(slug);
