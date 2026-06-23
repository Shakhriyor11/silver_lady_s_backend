-- Kategoriya tartibi uchun sort_order ustuni
ALTER TABLE categories ADD COLUMN IF NOT EXISTS sort_order INT NOT NULL DEFAULT 0;
UPDATE categories SET sort_order = CAST(id AS INT) WHERE sort_order = 0;

-- Subkategoriya uchun parent_id (o'z-o'ziga FK)
ALTER TABLE categories ADD COLUMN IF NOT EXISTS parent_id BIGINT;
ALTER TABLE categories
    ADD CONSTRAINT fk_category_parent
    FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL;

-- Eski global unique constraint olib tashlanadi (endi parent kontekstida tekshiriladi)
ALTER TABLE categories DROP CONSTRAINT IF EXISTS uk_category_name;

-- Root kategoriyalar uchun: bir xil ism faqat bir marta (parent_id IS NULL)
CREATE UNIQUE INDEX IF NOT EXISTS uk_cat_name_root
    ON categories (lower(name)) WHERE parent_id IS NULL;

-- Bir parent ichida: bir xil ism faqat bir marta
CREATE UNIQUE INDEX IF NOT EXISTS uk_cat_name_per_parent
    ON categories (parent_id, lower(name)) WHERE parent_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_category_sort_order ON categories (sort_order);
CREATE INDEX IF NOT EXISTS idx_category_parent_id  ON categories (parent_id);
