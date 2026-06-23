CREATE TABLE site_visits (
    id          BIGSERIAL PRIMARY KEY,
    visitor_id  VARCHAR(64)  NOT NULL,
    ip_hash     VARCHAR(64),
    visit_date  DATE         NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_sv_date        ON site_visits (visit_date);
CREATE INDEX idx_sv_visitor_date ON site_visits (visitor_id, visit_date);
