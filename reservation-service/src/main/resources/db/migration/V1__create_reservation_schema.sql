CREATE TABLE stock_items (
    sku VARCHAR(64) PRIMARY KEY,
    available_quantity INTEGER NOT NULL,
    CONSTRAINT stock_items_quantity_non_negative CHECK (available_quantity >= 0)
);

CREATE TABLE reservations (
    reservation_id UUID PRIMARY KEY,
    sku VARCHAR(64) NOT NULL,
    quantity INTEGER NOT NULL,
    reservation_status VARCHAR(16) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    released_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT reservations_stock_item_fk
        FOREIGN KEY (sku) REFERENCES stock_items (sku),
    CONSTRAINT reservations_quantity_range CHECK (quantity BETWEEN 1 AND 100),
    CONSTRAINT reservations_status_values
        CHECK (reservation_status IN ('CONFIRMED', 'RELEASED')),
    CONSTRAINT reservations_release_state CHECK (
        (reservation_status = 'CONFIRMED' AND released_at IS NULL)
        OR (reservation_status = 'RELEASED' AND released_at IS NOT NULL)
    ),
    CONSTRAINT reservations_release_chronology CHECK (
        released_at IS NULL OR released_at >= created_at
    )
);

CREATE INDEX reservations_sku_idx ON reservations (sku);
