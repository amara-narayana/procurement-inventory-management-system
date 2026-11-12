-- Auth Service Schema Migration V1

CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    department VARCHAR(50),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed Roles
INSERT INTO roles (id, name, description) VALUES
(1, 'ADMIN', 'System Administrator with full access'),
(2, 'PROCUREMENT_MANAGER', 'Procurement Manager overseeing requisitions and purchase orders'),
(3, 'PROCUREMENT_OFFICER', 'Procurement Officer handling suppliers and order processing'),
(4, 'INVENTORY_MANAGER', 'Inventory Manager responsible for stock and warehouse operations'),
(5, 'APPROVER', 'Designated Approver for requisitions and purchase orders'),
(6, 'EMPLOYEE', 'Standard Employee capable of creating purchase requisitions')
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- Default BCrypt hash for 'password123'
-- $2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00GDNzDFdut6t.
INSERT INTO users (id, username, password_hash, email, first_name, last_name, department, active) VALUES
(1, 'admin', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00GDNzDFdut6t.', 'admin@zycus.com', 'Admin', 'User', 'IT', TRUE),
(2, 'manager.proc', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00GDNzDFdut6t.', 'manager.proc@zycus.com', 'Procurement', 'Manager', 'Procurement', TRUE),
(3, 'officer.proc', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00GDNzDFdut6t.', 'officer.proc@zycus.com', 'Procurement', 'Officer', 'Procurement', TRUE),
(4, 'manager.inv', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00GDNzDFdut6t.', 'manager.inv@zycus.com', 'Inventory', 'Manager', 'Logistics', TRUE),
(5, 'approver1', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00GDNzDFdut6t.', 'approver1@zycus.com', 'Alice', 'Approver', 'Finance', TRUE),
(6, 'employee1', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00GDNzDFdut6t.', 'employee1@zycus.com', 'Bob', 'Employee', 'Engineering', TRUE)
ON DUPLICATE KEY UPDATE active = VALUES(active);

-- Seed User Roles
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1), -- admin -> ADMIN
(2, 2), -- manager.proc -> PROCUREMENT_MANAGER
(3, 3), -- officer.proc -> PROCUREMENT_OFFICER
(4, 4), -- manager.inv -> INVENTORY_MANAGER
(5, 5), -- approver1 -> APPROVER
(6, 6)  -- employee1 -> EMPLOYEE
ON DUPLICATE KEY UPDATE user_id = VALUES(user_id);
