-- Eski product_sizes jadvalini vaqtincha nusxalab saqlaymiz
ALTER TABLE product_sizes RENAME TO product_sizes_backup;

-- Yangi jadval: har o'lcham uchun alohida miqdor
CREATE TABLE product_size_stock (
    id          BIGSERIAL PRIMARY KEY,
    product_id  BIGINT       NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    size        VARCHAR(30)  NOT NULL,
    quantity    INT          NOT NULL DEFAULT 0,
    sort_order  INT          NOT NULL DEFAULT 0,
    CONSTRAINT uk_product_size UNIQUE (product_id, size)
);

-- Mavjud ma'lumotlarni ko'chirish
-- Har o'lcham uchun miqdor sifatida mahsulotning stock_quantity ni olamiz
-- (Haqiqiy miqdor noma'lum, admin keyinchalik yangilaydi)
INSERT INTO product_size_stock (product_id, size, quantity, sort_order)
SELECT ps.product_id, ps.size, GREATEST(p.stock_quantity, 1), ps.size_order
FROM product_sizes_backup ps
JOIN products p ON p.id = ps.product_id;

DROP TABLE product_sizes_backup;

CREATE INDEX idx_size_stock_product ON product_size_stock(product_id);
CREATE INDEX idx_size_stock_size    ON product_size_stock(size);
