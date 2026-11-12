# REST API Documentation

All API endpoints are accessible directly via **API Gateway (Port 8080)** or on individual service ports.
Interactive OpenAPI / Swagger UI is available at:
- `http://localhost:8081/swagger-ui.html` (Auth Service)
- `http://localhost:8082/swagger-ui.html` (Inventory Service)
- `http://localhost:8083/swagger-ui.html` (Procurement Service)

---

## 1. Authentication Service (`/api/auth`)

### 1.1 Register User
- **Method / Endpoint**: `POST /api/auth/register`
- **Access**: Public
- **Request Body**:
```json
{
  "username": "jdoe",
  "password": "Password123!",
  "email": "jdoe@company.com",
  "firstName": "John",
  "lastName": "Doe",
  "department": "Engineering"
}
```
- **Response**: `200 OK`
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "uuid-token...",
    "tokenType": "Bearer",
    "userId": 10,
    "username": "jdoe",
    "email": "jdoe@company.com",
    "roles": ["EMPLOYEE"]
  }
}
```

### 1.2 User Login
- **Method / Endpoint**: `POST /api/auth/login`
- **Access**: Public
- **Request Body**:
```json
{
  "username": "admin",
  "password": "password123"
}
```
- **Response**: `200 OK` (returns JWT token and assigned roles)

### 1.3 Get Current User Profile
- **Method / Endpoint**: `GET /api/auth/me`
- **Access**: Authenticated (`Bearer <token>`)

### 1.4 List All Users
- **Method / Endpoint**: `GET /api/auth/users`
- **Access**: `ADMIN`

### 1.5 Update User Roles
- **Method / Endpoint**: `PUT /api/auth/users/{userId}/roles`
- **Access**: `ADMIN`
- **Request Body**:
```json
{
  "roles": ["EMPLOYEE", "APPROVER"]
}
```

---

## 2. Inventory Service (`/api/inventory`)

### 2.1 Product Catalog
- `GET /api/inventory/products`: List products with pagination, category filter, keyword search, active status filter.
- `GET /api/inventory/products/{id}`: Retrieve single product.
- `POST /api/inventory/products`: Create product (`ADMIN`, `INVENTORY_MANAGER`).
- `PUT /api/inventory/products/{id}`: Update product (`ADMIN`, `INVENTORY_MANAGER`).
- `DELETE /api/inventory/products/{id}`: Soft delete / deactivate product (`ADMIN`).

### 2.2 Categories & Warehouses
- `GET /api/inventory/categories`: List all categories.
- `POST /api/inventory/categories`: Create category (`ADMIN`, `INVENTORY_MANAGER`).
- `GET /api/inventory/warehouses`: List all warehouses.
- `POST /api/inventory/warehouses`: Create warehouse (`ADMIN`, `INVENTORY_MANAGER`).
- `PUT /api/inventory/warehouses/{id}`: Update warehouse (`ADMIN`, `INVENTORY_MANAGER`).

### 2.3 Stock & Inventory Control
- `GET /api/inventory/stock`: List inventory across all products and warehouses.
- `GET /api/inventory/stock/low-stock`: List stock items below defined reorder thresholds.
- `POST /api/inventory/stock/adjustments`: Apply physical inventory adjustment (`ADD` or `SUBTRACT`).
```json
{
  "productId": 1,
  "warehouseId": 1,
  "adjustmentType": "ADD",
  "quantity": 10,
  "reason": "Cycle count reconciliation"
}
```
- `GET /api/inventory/stock/movements`: View stock movement audit history.
- `POST /api/inventory/stock/goods-receipt`: Synchronize inbound goods receipt (`INVENTORY_MANAGER`, `ADMIN`, internal service).

---

## 3. Procurement Service (`/api/procurement`)

### 3.1 Supplier Directory
- `GET /api/procurement/suppliers`: List suppliers with status filtering.
- `GET /api/procurement/suppliers/{id}`: Retrieve supplier details.
- `POST /api/procurement/suppliers`: Onboard new supplier (`ADMIN`, `PROCUREMENT_MANAGER`, `PROCUREMENT_OFFICER`).
- `PUT /api/procurement/suppliers/{id}`: Update supplier details.

### 3.2 Purchase Requisitions
- `POST /api/procurement/requisitions`: Create new purchase requisition.
```json
{
  "department": "Engineering",
  "businessJustification": "Cloud engineering workstations",
  "items": [
    {
      "productId": 1,
      "quantity": 5,
      "estimatedUnitPrice": 1200.00
    }
  ]
}
```
- `GET /api/procurement/requisitions`: List requisitions with status filter.
- `GET /api/procurement/requisitions/{id}`: Get requisition details and line items.
- `POST /api/procurement/requisitions/{id}/submit`: Submit requisition for approval.
- `POST /api/procurement/requisitions/{id}/approve`: Approve requisition (`APPROVER`, `PROCUREMENT_MANAGER`, `ADMIN`). *Enforces segregation of duties (requester cannot self-approve).*
- `POST /api/procurement/requisitions/{id}/reject`: Reject requisition with required rejection notes.

### 3.3 Purchase Orders
- `POST /api/procurement/orders`: Create direct purchase order.
- `POST /api/procurement/orders/from-requisition/{requisitionId}?supplierId={supplierId}`: Convert approved requisition to purchase order.
- `GET /api/procurement/orders`: List purchase orders with status filter.
- `GET /api/procurement/orders/{id}`: Get order details and fulfillment quantities.
- `POST /api/procurement/orders/{id}/approve`: Approve purchase order.
- `POST /api/procurement/orders/{id}/send`: Mark order as issued/dispatched to supplier.
- `POST /api/procurement/orders/{id}/cancel`: Cancel purchase order.

### 3.4 Goods Receipt
- `POST /api/procurement/orders/{id}/receipts`: Record goods received against PO.
```json
{
  "warehouseId": 1,
  "notes": "Delivered via Freight Carrier, BOL #99120",
  "items": [
    {
      "poItemId": 1,
      "productId": 1,
      "quantityReceived": 5,
      "notes": "Verified condition OK"
    }
  ]
}
```
- `GET /api/procurement/orders/receipts/all`: List all goods receipt records.
- `GET /api/procurement/orders/{id}/receipts`: List goods receipts for a specific purchase order.

### 3.5 Reports & Analytics
- `GET /api/procurement/reports/dashboard`: Executive analytics summary (pending PRs, open POs, total spend, spend by vendor, status distributions).
