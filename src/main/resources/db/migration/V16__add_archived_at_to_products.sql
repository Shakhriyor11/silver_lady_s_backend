ALTER TABLE products
    ADD COLUMN IF NOT EXISTS archived_at TIMESTAMP(6) WITH TIME ZONE;

-- Avval arxivlangan mahsulotlar uchun eng yaqin taxmin: oxirgi yangilangan vaqt
UPDATE products SET archived_at = updated_at WHERE active = false AND archived_at IS NULL;
