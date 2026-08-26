CREATE TABLE IF NOT EXISTS sales (
    id            BIGSERIAL PRIMARY KEY,
    cashier_id    BIGINT NOT NULL REFERENCES users(id),
    total_amount  NUMERIC(19,2) NOT NULL,
    created_at    timestamp(6) with time zone NOT NULL DEFAULT now(),
    updated_at    timestamp(6) with time zone NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_sales_cashier_id ON sales(cashier_id);
CREATE INDEX IF NOT EXISTS idx_sales_created_at ON sales(created_at);

CREATE TABLE IF NOT EXISTS sale_items (
    id            BIGSERIAL PRIMARY KEY,
    sale_id       BIGINT NOT NULL REFERENCES sales(id) ON DELETE CASCADE,
    product_id    BIGINT REFERENCES products(id) ON DELETE SET NULL,
    product_name  VARCHAR(160) NOT NULL,
    size          VARCHAR(30) NOT NULL,
    unit_price    NUMERIC(19,2) NOT NULL,
    quantity      INTEGER NOT NULL,
    line_total    NUMERIC(19,2) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_sale_items_sale_id ON sale_items(sale_id);
