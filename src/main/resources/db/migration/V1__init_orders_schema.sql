-- Create schema
CREATE SCHEMA IF NOT EXISTS orders;

-- Order status table
CREATE TABLE orders.status (
	id SMALLINT PRIMARY KEY,
	status VARCHAR(50) NOT NULL UNIQUE
);

-- status
INSERT INTO orders.status (id, status) VALUES
	(0, 'CREATED'), 
	(1, 'CANCELLED'), 
	(2, 'SHIPPED'),
	(3, 'CONFIRMED');

-- Product table
CREATE TABLE orders.products (
    id BIGSERIAL PRIMARY KEY,
    sku VARCHAR(100) NOT NULL UNIQUE,
	external_id uuid NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    active BOOLEAN DEFAULT TRUE,
    amount NUMERIC(19,4) NOT NULL, -- Money.amount
    currency VARCHAR(3) -- Money.currency
);

COMMENT ON COLUMN orders.products.sku
	IS 'External unique product identifier';

COMMENT ON COLUMN orders.products.external_id
	IS 'External unique order identifier (UUID)';

-- Indexes
DROP INDEX IF EXISTS idx_products_external_id;
CREATE INDEX idx_products_external_id ON orders.products(external_id);

-- Order table
CREATE TABLE orders.orders (
    id BIGSERIAL PRIMARY KEY,
    external_id UUID NOT NULL UNIQUE,
    customer_external_id UUID NOT NULL,
    status_id SMALLINT NOT NULL REFERENCES orders.status(id),
    total_amount NUMERIC(19,4), -- Money.amount
    currency VARCHAR(3), -- Money.currency
    created TIMESTAMPTZ DEFAULT NOW()
);

-- Indexes
DROP INDEX IF EXISTS idx_orders_external_id;
CREATE INDEX idx_orders_external_id ON orders.orders(external_id);

DROP INDEX IF EXISTS idx_orders_customer_id;
CREATE INDEX idx_orders_customer_id ON orders.orders(customer_external_id);

COMMENT ON COLUMN orders.orders.external_id
	IS 'External unique order identifier (UUID)';

COMMENT ON COLUMN orders.orders.customer_external_id
	IS 'External unique user identifier (UUID)';

COMMENT ON COLUMN orders.orders.total_amount
	IS 'Total amount for the whole order';

-- OrderLines table
CREATE TABLE orders.order_lines (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders.orders(id),
    product_id BIGINT NOT NULL REFERENCES orders.products(id),
    product_name VARCHAR(255) NOT NULL,
    unit_price NUMERIC(19,4) NOT NULL,
    currency VARCHAR(3),
    quantity INT NOT NULL,
    line_total NUMERIC(19,4) NOT NULL
);

COMMENT ON COLUMN orders.order_lines.product_name
	IS 'Snapshot of product name at the time it was added to the order';

COMMENT ON COLUMN orders.order_lines.unit_price
	IS 'Snapshot of product price at the time it was added to the order';

COMMENT ON COLUMN orders.order_lines.quantity
	IS 'Product quantity added to the order';

COMMENT ON COLUMN orders.order_lines.line_total
	IS 'Order line total (unit_price * quantity)';

DROP INDEX IF EXISTS idx_orderline_order_id;
CREATE INDEX idx_orderline_order_id ON orders.order_lines(order_id);

COMMIT;
