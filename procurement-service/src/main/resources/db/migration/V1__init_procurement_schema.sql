-- Procurement Service Schema Migration V1

CREATE TABLE IF NOT EXISTS suppliers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    contact_person VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    address VARCHAR(255) NOT NULL,
    tax_id VARCHAR(50) NOT NULL UNIQUE,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS purchase_requisitions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    requisition_number VARCHAR(50) NOT NULL UNIQUE,
    requesting_user VARCHAR(50) NOT NULL,
    department VARCHAR(50) NOT NULL,
    business_justification TEXT NOT NULL,
    total_estimated_amount DECIMAL(14, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    submission_date TIMESTAMP NULL,
    approver_user VARCHAR(50) NULL,
    approval_date TIMESTAMP NULL,
    approval_notes TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS requisition_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    requisition_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_sku VARCHAR(50) NOT NULL,
    product_name VARCHAR(150) NOT NULL,
    quantity INT NOT NULL,
    estimated_unit_price DECIMAL(12, 2) NOT NULL,
    line_total DECIMAL(14, 2) NOT NULL,
    CONSTRAINT fk_req_items_requisition FOREIGN KEY (requisition_id) REFERENCES purchase_requisitions (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS purchase_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    po_number VARCHAR(50) NOT NULL UNIQUE,
    requisition_id BIGINT NULL,
    supplier_id BIGINT NOT NULL,
    total_amount DECIMAL(14, 2) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expected_delivery_date TIMESTAMP NULL,
    notes TEXT NULL,
    created_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_po_requisition FOREIGN KEY (requisition_id) REFERENCES purchase_requisitions (id) ON DELETE SET NULL,
    CONSTRAINT fk_po_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS purchase_order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    purchase_order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_sku VARCHAR(50) NOT NULL,
    product_name VARCHAR(150) NOT NULL,
    quantity_ordered INT NOT NULL,
    quantity_received INT NOT NULL DEFAULT 0,
    unit_price DECIMAL(12, 2) NOT NULL,
    line_total DECIMAL(14, 2) NOT NULL,
    CONSTRAINT fk_po_items_order FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS goods_receipts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    receipt_number VARCHAR(50) NOT NULL UNIQUE,
    purchase_order_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    received_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    received_by VARCHAR(50) NOT NULL,
    notes TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_receipt_po FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS goods_receipt_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    goods_receipt_id BIGINT NOT NULL,
    po_item_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_sku VARCHAR(50) NOT NULL,
    quantity_received INT NOT NULL,
    notes VARCHAR(255) NULL,
    CONSTRAINT fk_receipt_items_receipt FOREIGN KEY (goods_receipt_id) REFERENCES goods_receipts (id) ON DELETE CASCADE,
    CONSTRAINT fk_receipt_items_po_item FOREIGN KEY (po_item_id) REFERENCES purchase_order_items (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed Suppliers
INSERT INTO suppliers (id, name, contact_person, email, phone, address, tax_id, status) VALUES
(1, 'Global Tech Supply Corp', 'Robert Vance', 'robert.v@globaltech.com', '+1-555-019-2831', '400 Technology Way, Austin TX', 'TAX-US-992101', 'ACTIVE'),
(2, 'Apex Office & Furniture Ltd', 'Evelyn Hayes', 'ehayes@apexoffice.com', '+1-555-014-9922', '12 Industrial Parkway, Cleveland OH', 'TAX-US-881203', 'ACTIVE'),
(3, 'Industrial Fasteners Direct', 'David Miller', 'dmiller@fastenersdirect.com', '+1-555-018-7711', '75 Foundry Street, Pittsburgh PA', 'TAX-US-773409', 'ACTIVE'),
(4, 'Packaging Solutions Unlimited', 'Laura Croft', 'lcroft@packagingsolutions.com', '+1-555-012-3344', '99 Harbor View Drive, Seattle WA', 'TAX-US-664512', 'ACTIVE')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Seed Sample Approved Requisition
INSERT INTO purchase_requisitions (id, requisition_number, requesting_user, department, business_justification, total_estimated_amount, status, submission_date, approver_user, approval_date, approval_notes) VALUES
(1, 'REQ-2026-0001', 'employee1', 'Engineering', 'New developer laptops for incoming engineering team members', 6250.00, 'APPROVED', CURRENT_TIMESTAMP, 'approver1', CURRENT_TIMESTAMP, 'Approved in accordance with Q4 IT equipment allocation budget');

INSERT INTO requisition_items (requisition_id, product_id, product_sku, product_name, quantity, estimated_unit_price, line_total) VALUES
(1, 1, 'PROD-LAPTOP-01', 'Enterprise Ultrabook 14-inch', 5, 1250.00, 6250.00);

-- Seed Sample Purchase Order
INSERT INTO purchase_orders (id, po_number, requisition_id, supplier_id, total_amount, status, expected_delivery_date, notes, created_by) VALUES
(1, 'PO-2026-0001', 1, 1, 6250.00, 'SENT', DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 14 DAY), 'Standard delivery to Central Distribution Center', 'manager.proc');

INSERT INTO purchase_order_items (purchase_order_id, product_id, product_sku, product_name, quantity_ordered, quantity_received, unit_price, line_total) VALUES
(1, 1, 'PROD-LAPTOP-01', 'Enterprise Ultrabook 14-inch', 5, 0, 1250.00, 6250.00);
