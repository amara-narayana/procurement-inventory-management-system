-- Inventory Service Schema Migration V1

CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    category_id BIGINT,
    unit_of_measurement VARCHAR(30) NOT NULL,
    unit_price DECIMAL(12, 2) NOT NULL,
    reorder_threshold INT NOT NULL DEFAULT 10,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS warehouses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    location VARCHAR(200) NOT NULL,
    description VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS inventory_stock (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_product_warehouse UNIQUE (product_id, warehouse_id),
    CONSTRAINT fk_stock_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE RESTRICT,
    CONSTRAINT fk_stock_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses (id) ON DELETE RESTRICT,
    CONSTRAINT chk_positive_quantity CHECK (quantity >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS stock_movements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    movement_type VARCHAR(50) NOT NULL,
    quantity INT NOT NULL,
    previous_quantity INT NOT NULL,
    new_quantity INT NOT NULL,
    reference_number VARCHAR(100),
    reason VARCHAR(255),
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mov_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE RESTRICT,
    CONSTRAINT fk_mov_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed Categories
INSERT INTO categories (id, name, description, active) VALUES
(1, 'Electronics & IT', 'Computing hardware, networking accessories and peripherals', TRUE),
(2, 'Office Supplies', 'Stationery, paper supplies, desk furniture and office consumables', TRUE),
(3, 'Industrial Hardware', 'Fasteners, mechanical fittings, tools and safety apparatus', TRUE),
(4, 'Packaging Materials', 'Corrugated boxes, protective wrap, strapping and labels', TRUE)
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- Seed Warehouses
INSERT INTO warehouses (id, name, location, description, active) VALUES
(1, 'Central Distribution Center', 'Building 4, Logistics Park East, Chicago IL', 'Primary hub for inbound procurement and distribution', TRUE),
(2, 'West Coast Fulfillment Hub', '88 Gateway Blvd, Reno NV', 'Secondary logistics point for regional fulfillment', TRUE),
(3, 'Midwest Storage Annex', '12 Terminal Road, Indianapolis IN', 'Bulk material and overflow storage facility', TRUE)
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- Seed Products
INSERT INTO products (id, sku, name, description, category_id, unit_of_measurement, unit_price, reorder_threshold, active) VALUES
(1, 'PROD-LAPTOP-01', 'Enterprise Ultrabook 14-inch', 'High performance enterprise business laptop with 16GB RAM', 1, 'PIECE', 1250.00, 15, TRUE),
(2, 'PROD-MONITOR-02', 'Ultra-Wide 27-inch IPS Monitor', 'WQHD Ergonomic workspace display with USB-C hub', 1, 'PIECE', 340.00, 20, TRUE),
(3, 'PROD-CHAIR-03', 'Ergonomic Task Chair', 'Lumbar support mesh desk chair with adjustable arms', 2, 'PIECE', 280.00, 10, TRUE),
(4, 'PROD-FASTENER-04', 'Grade-8 Industrial Hex Bolt Box', 'Heavy duty fasteners zinc coated M10 x 50mm pack of 100', 3, 'BOX', 45.50, 30, TRUE),
(5, 'PROD-BOX-05', 'Heavy Duty Shipping Carton Large', 'Double-wall corrugated shipping boxes 24x18x18 bundle of 25', 4, 'BUNDLE', 62.00, 25, TRUE)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Seed Initial Inventory Stock
INSERT INTO inventory_stock (id, product_id, warehouse_id, quantity, version) VALUES
(1, 1, 1, 35, 0), -- Laptop @ Central: 35
(2, 1, 2, 12, 0), -- Laptop @ West Coast: 12
(3, 2, 1, 40, 0), -- Monitor @ Central: 40
(4, 3, 1, 8, 0),  -- Chair @ Central: 8 (Below reorder threshold 10 -> Low stock!)
(5, 4, 3, 100, 0),-- Fastener @ Storage: 100
(6, 5, 1, 50, 0)  -- Box @ Central: 50
ON DUPLICATE KEY UPDATE quantity = VALUES(quantity);

-- Seed Initial Stock Movements
INSERT INTO stock_movements (product_id, warehouse_id, movement_type, quantity, previous_quantity, new_quantity, reference_number, reason, created_by) VALUES
(1, 1, 'INITIAL_STOCK', 35, 0, 35, 'INIT-001', 'Initial inventory intake', 'admin'),
(1, 2, 'INITIAL_STOCK', 12, 0, 12, 'INIT-002', 'Initial inventory intake', 'admin'),
(2, 1, 'INITIAL_STOCK', 40, 0, 40, 'INIT-003', 'Initial inventory intake', 'admin'),
(3, 1, 'INITIAL_STOCK', 8, 0, 8, 'INIT-004', 'Initial inventory intake', 'admin'),
(4, 3, 'INITIAL_STOCK', 100, 0, 100, 'INIT-005', 'Initial inventory intake', 'admin'),
(5, 1, 'INITIAL_STOCK', 50, 0, 50, 'INIT-006', 'Initial inventory intake', 'admin');
