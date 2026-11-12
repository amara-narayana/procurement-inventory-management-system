# Database Design & Architecture Specification

## 1. Overview & Principles

The Procurement & Inventory Management System (PIMS) adopts a **Database-per-Service** architectural pattern to ensure loose coupling, independent scalability, and domain isolation.
Each microservice governs its own MySQL 8.x schema via Flyway migrations:

1. `pims_auth_db`: Identity, credentials, and role assignments.
2. `pims_inventory_db`: Warehouses, product catalog, stock records, and movement audit log.
3. `pims_procurement_db`: Suppliers, purchase requisitions, purchase orders, approval records, and goods receipts.

## 2. Global Conventions
- Primary keys are `BIGINT AUTO_INCREMENT` (or `INT AUTO_INCREMENT` for reference dictionaries).
- Audit timestamps (`created_at`, `updated_at`) are automatically set with `TIMESTAMP DEFAULT CURRENT_TIMESTAMP`.
- Financial amounts and monetary values use `DECIMAL(14, 2)` or `DECIMAL(12, 2)` to eliminate floating-point rounding errors.
- Quantities use `INT NOT NULL` with application- and schema-level non-negative constraints.
- Foreign keys maintain referential integrity with appropriate `ON DELETE RESTRICT` or `ON DELETE CASCADE` cascading where appropriate.
- Database schema changes are tracked strictly via Flyway migration scripts in each service (`src/main/resources/db/migration/V1__*.sql`).

---

## 3. Schemas & Entity-Relationship Details

### 3.1 Auth Database (`pims_auth_db`)

```mermaid
erDiagram
    USERS ||--o{ USER_ROLES : has
    ROLES ||--o{ USER_ROLES : assigned_to

    USERS {
        BIGINT id PK
        VARCHAR username UK
        VARCHAR password
        VARCHAR email UK
        VARCHAR first_name
        VARCHAR last_name
        VARCHAR department
        BOOLEAN active
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    ROLES {
        INT id PK
        VARCHAR name UK
        VARCHAR description
    }

    USER_ROLES {
        BIGINT user_id PK, FK
        INT role_id PK, FK
    }
```

#### Roles Seeded:
- `ADMIN`: Full platform authority and user administration.
- `PROCUREMENT_MANAGER`: Requisition approvals, PO oversight, vendor management.
- `PROCUREMENT_OFFICER`: PO creation, supplier management, vendor dispatch.
- `INVENTORY_MANAGER`: Product catalog, warehouse management, stock adjustments.
- `APPROVER`: Requisition approval queue review and authorization.
- `EMPLOYEE`: Requisition creation and personal tracking.

---

### 3.2 Inventory Database (`pims_inventory_db`)

```mermaid
erDiagram
    CATEGORIES ||--o{ PRODUCTS : categorizes
    PRODUCTS ||--o{ INVENTORY_STOCK : stocked_in
    WAREHOUSES ||--o{ INVENTORY_STOCK : stores
    PRODUCTS ||--o{ STOCK_MOVEMENTS : tracked_in
    WAREHOUSES ||--o{ STOCK_MOVEMENTS : located_at

    CATEGORIES {
        INT id PK
        VARCHAR name UK
        VARCHAR description
        BOOLEAN active
        TIMESTAMP created_at
    }

    PRODUCTS {
        BIGINT id PK
        VARCHAR sku UK
        VARCHAR name
        VARCHAR description
        INT category_id FK
        VARCHAR unit_of_measurement
        DECIMAL unit_price
        INT reorder_threshold
        BOOLEAN active
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    WAREHOUSES {
        BIGINT id PK
        VARCHAR name
        VARCHAR location
        VARCHAR description
        BOOLEAN active
        TIMESTAMP created_at
    }

    INVENTORY_STOCK {
        BIGINT id PK
        BIGINT product_id FK
        BIGINT warehouse_id FK
        INT quantity
        BIGINT version
        TIMESTAMP updated_at
    }

    STOCK_MOVEMENTS {
        BIGINT id PK
        BIGINT product_id FK
        BIGINT warehouse_id FK
        VARCHAR movement_type
        INT quantity
        INT previous_quantity
        INT new_quantity
        VARCHAR reference_number
        VARCHAR reason
        VARCHAR created_by
        TIMESTAMP created_at
    }
```

#### Optimistic Locking & Concurrency Control
- `inventory_stock` features a `version BIGINT DEFAULT 0` column annotated with `@Version` in JPA.
- When concurrent transactions (e.g. concurrent goods receipts or manual stock adjustments) update the stock of the same product at the same warehouse, JPA checks the version and raises an `OptimisticLockingFailureException`, preventing lost updates.

---

### 3.3 Procurement Database (`pims_procurement_db`)

```mermaid
erDiagram
    SUPPLIERS ||--o{ PURCHASE_ORDERS : fulfills
    PURCHASE_REQUISITIONS ||--o{ REQUISITION_ITEMS : contains
    PURCHASE_REQUISITIONS ||--o{ PURCHASE_ORDERS : originates
    PURCHASE_ORDERS ||--o{ PURCHASE_ORDER_ITEMS : contains
    PURCHASE_ORDERS ||--o{ GOODS_RECEIPTS : receives
    GOODS_RECEIPTS ||--o{ GOODS_RECEIPT_ITEMS : records

    SUPPLIERS {
        BIGINT id PK
        VARCHAR name UK
        VARCHAR contact_person
        VARCHAR email UK
        VARCHAR phone
        VARCHAR address
        VARCHAR tax_id UK
        VARCHAR status
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    PURCHASE_REQUISITIONS {
        BIGINT id PK
        VARCHAR requisition_number UK
        VARCHAR requesting_user
        VARCHAR department
        VARCHAR business_justification
        DECIMAL total_estimated_amount
        VARCHAR status
        TIMESTAMP submission_date
        VARCHAR approver_user
        TIMESTAMP approval_date
        VARCHAR approval_notes
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    REQUISITION_ITEMS {
        BIGINT id PK
        BIGINT requisition_id FK
        BIGINT product_id
        VARCHAR product_sku
        VARCHAR product_name
        INT quantity
        DECIMAL estimated_unit_price
        DECIMAL line_total
    }

    PURCHASE_ORDERS {
        BIGINT id PK
        VARCHAR po_number UK
        BIGINT requisition_id FK
        VARCHAR requisition_number
        BIGINT supplier_id FK
        VARCHAR supplier_name
        DECIMAL total_amount
        VARCHAR status
        TIMESTAMP order_date
        TIMESTAMP expected_delivery_date
        VARCHAR notes
        VARCHAR created_by
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    PURCHASE_ORDER_ITEMS {
        BIGINT id PK
        BIGINT purchase_order_id FK
        BIGINT product_id
        VARCHAR product_sku
        VARCHAR product_name
        INT quantity_ordered
        INT quantity_received
        DECIMAL unit_price
        DECIMAL line_total
    }

    GOODS_RECEIPTS {
        BIGINT id PK
        VARCHAR receipt_number UK
        BIGINT purchase_order_id FK
        VARCHAR po_number
        BIGINT warehouse_id
        TIMESTAMP received_date
        VARCHAR received_by
        VARCHAR notes
        TIMESTAMP created_at
    }

    GOODS_RECEIPT_ITEMS {
        BIGINT id PK
        BIGINT goods_receipt_id FK
        BIGINT po_item_id FK
        BIGINT product_id
        VARCHAR product_sku
        INT quantity_received
        VARCHAR notes
    }
```

---

## 4. Key Integrity & Business Rules
1. **Segregation of Duties**: In `purchase_requisitions`, a user cannot approve their own requisition (`requestingUser != approverUser`).
2. **Deterministic State Progression**:
   - Requisition: `DRAFT` → `SUBMITTED` → `APPROVED` / `REJECTED` → `CONVERTED_TO_ORDER` / `CANCELLED`.
   - Purchase Order: `DRAFT` → `APPROVED` → `SENT` → `PARTIALLY_RECEIVED` → `RECEIVED` / `CANCELLED`.
3. **Goods Receipt Over-Receipt Prevention**: `quantityReceived` for any line item cannot exceed `(quantityOrdered - quantityReceived)`.
4. **Inter-Service Stock Ingestion**: Recording a Goods Receipt transactionally validates receipt quantities in `procurement-service` and calls `POST /api/inventory/stock/goods-receipt` in `inventory-service` to augment warehouse stock and write audit movement entries.
