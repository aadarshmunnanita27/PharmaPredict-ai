CREATE TABLE medicines (
    id BIGSERIAL PRIMARY KEY,
    sku VARCHAR(40) NOT NULL UNIQUE,
    name VARCHAR(160) NOT NULL,
    category VARCHAR(80) NOT NULL,
    manufacturer VARCHAR(120) NOT NULL,
    unit_price NUMERIC(12,2) NOT NULL CHECK (unit_price >= 0),
    stock_qty INTEGER NOT NULL CHECK (stock_qty >= 0),
    reorder_level INTEGER NOT NULL CHECK (reorder_level >= 0),
    lead_time_days INTEGER NOT NULL CHECK (lead_time_days >= 1),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE sales (
    id BIGSERIAL PRIMARY KEY,
    medicine_id BIGINT NOT NULL REFERENCES medicines(id),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    sold_at DATE NOT NULL
);

CREATE INDEX idx_sales_medicine_date ON sales(medicine_id, sold_at);
CREATE INDEX idx_sales_sold_at ON sales(sold_at);
CREATE INDEX idx_medicines_active_stock ON medicines(active, stock_qty);