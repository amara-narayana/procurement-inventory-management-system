# Procurement & Inventory Management System (PIMS)

An enterprise-grade, distributed microservices platform engineered with **Java 17/21**, **Spring Boot 3.3.4**, **Spring Security (JWT)**, **MySQL 8.x**, **Flyway**, and **React 18** with **TypeScript** and **Vite**.

---

## 1. Architectural Highlights

- **Maven Multi-Module Architecture**:
  - `api-gateway`: Spring Cloud Gateway (Port 8080) with centralized routing and CORS.
  - `auth-service`: Authentication, BCrypt password hashing, JWT generation, and RBAC (Port 8081).
  - `inventory-service`: Catalog, warehouses, optimistic-locked stock tracking, and movement logs (Port 8082).
  - `procurement-service`: Suppliers, purchase requisitions, approval queue, purchase orders, goods receipts, and spend analytics (Port 8083).
  - `common-library`: Shared DTOs, Enums, JWT filters, custom exceptions, and unified error responses.
  - `frontend`: React 18, TypeScript 5, Vite, Axios client with JWT interceptors, responsive enterprise UI (Port 5173).
- **Database-Per-Service**: Separate MySQL 8 schemas (`pims_auth_db`, `pims_inventory_db`, `pims_procurement_db`) managed via Flyway migrations.
- **Enterprise Controls**:
  - **Segregation of Duties**: Requesters are strictly prohibited from approving their own purchase requisitions.
  - **Optimistic Locking**: JPA `@Version` concurrency control on inventory stock records prevents race conditions during simultaneous adjustments or shipments.
  - **Over-Delivery Protection**: Goods receipt processing validates line items against pending ordered quantities.
  - **Full Audit Trail**: Every stock change (receipt, adjustment, outbound) writes an immutable stock movement record.

---

## 2. Technology Stack

- **Backend**: Java 17/21, Spring Boot 3.3.4, Spring MVC, Spring Data JPA, Hibernate ORM, Spring Security 6, JJWT 0.12.6, Spring Cloud Gateway 4.1.5, Springdoc OpenAPI 2.6.0.
- **Database**: MySQL 8.x, Flyway Migration 10.10.0, H2 Database (for isolated unit/integration tests).
- **Frontend**: React 18, TypeScript 5, Vite, Axios, React Router 6.
- **Testing**: JUnit 5, Mockito, Spring Boot Test, MockMvc.

---

## 3. Getting Started

### 3.1 Prerequisites
- **Java Development Kit**: JDK 17 or 21 (`java -version`)
- **Apache Maven**: 3.9+ (`mvn -version`)
- **Node.js**: 18+ and npm (`node -v`, `npm -v`)
- **MySQL**: 8.0+ running on `localhost:3306`

### 3.2 Database Initialization
Ensure your MySQL server is running. Create the databases using your preferred MySQL client:
```sql
CREATE DATABASE IF NOT EXISTS pims_auth_db;
CREATE DATABASE IF NOT EXISTS pims_inventory_db;
CREATE DATABASE IF NOT EXISTS pims_procurement_db;
```
*(Default credentials are set to `root` / `1234` in `application.yml`. You can override them via environment variables or `.env`.)*

Flyway will automatically apply migrations upon microservice startup.

### 3.3 Build the Complete Project
Run the Maven reactor build across all backend modules:
```bash
mvn clean test package
```

Build the frontend bundle:
```bash
cd frontend
npm install
npm run build
```

---

## 4. Running the System

Open separate terminal windows to launch each microservice:

```bash
# Terminal 1: Auth Service (Port 8081)
mvn -pl auth-service spring-boot:run

# Terminal 2: Inventory Service (Port 8082)
mvn -pl inventory-service spring-boot:run

# Terminal 3: Procurement Service (Port 8083)
mvn -pl procurement-service spring-boot:run

# Terminal 4: API Gateway (Port 8080)
mvn -pl api-gateway spring-boot:run

# Terminal 5: Frontend (Port 5173)
cd frontend
npm run dev
```

Open your browser and navigate to:
👉 **`http://localhost:5173`**

---

## 5. Pre-Configured Demo Accounts

All seed user accounts use the password: **`password123`**

| Username | Role | Primary Functionality |
| :--- | :--- | :--- |
| `admin` | `ADMIN` | System administration, role assignments, full access |
| `manager.proc` | `PROCUREMENT_MANAGER` | Authorize requisitions, manage purchase orders, view spend |
| `officer.proc` | `PROCUREMENT_OFFICER` | Supplier onboarding, convert approved PRs to POs, dispatch POs |
| `manager.inv` | `INVENTORY_MANAGER` | Product catalog, warehouses, stock adjustments, goods receipts |
| `approver1` | `APPROVER` | Requisition approval queue review and authorization |
| `employee1` | `EMPLOYEE` | Create and submit departmental purchase requisitions |

---

## 6. End-to-End Procurement Workflow Walkthrough

1. **Submit Requisition**:
   - Log in as **`employee1`**.
   - Navigate to **Requisitions** → Click **+ New Requisition**.
   - Add line items (e.g. 5x `IT-LAP-001 Enterprise Laptop`), specify business justification, and click **Save Requisition**.
   - On the draft card, click **Submit**.
2. **Review & Authorize (Segregation of Duties)**:
   - Log out and log in as **`approver1`** (or `manager.proc`).
   - Navigate to **Approvals Queue**.
   - Click **Review & Decide** on the submitted requisition. Enter decision notes and click **Approve Requisition**.
3. **Convert to Purchase Order & Dispatch**:
   - Log in as **`officer.proc`**.
   - Navigate to **Purchase Orders** → Click **Convert PR to PO**.
   - Select the approved requisition and choose a vendor (e.g. `Dell Technologies Inc.`).
   - Click **Convert to PO**.
   - Review the generated PO, click **Approve**, and then click **Send PO** to dispatch to the vendor.
4. **Fulfill & Ingest via Goods Receipt**:
   - Log in as **`manager.inv`**.
   - Navigate to **Goods Receipt**.
   - The pending PO will appear under *Pending Orders Awaiting Delivery*.
   - Click **Ingest Receipt**, select destination warehouse, verify quantities received, and click **Confirm Receipt & Ingest Stock**.
   - The system synchronously updates warehouse inventory and creates an immutable audit trail in `stock_movements`.
5. **Analyze Analytics & Audit**:
   - Navigate to **Stock & Movements** to inspect real-time quantities and transaction audit history.
   - Navigate to **Reports & Spend** to inspect spend broken down by supplier and PO fulfillment status.

---

## 7. Interactive API Documentation

Interactive Swagger UI interfaces:
- **Auth Service**: [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)
- **Inventory Service**: [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html)
- **Procurement Service**: [http://localhost:8083/swagger-ui.html](http://localhost:8083/swagger-ui.html)

---

## 8. Verification & Test Suite

To run all 30+ unit, repository, and controller integration tests across the reactor:
```bash
mvn test
```
All integration tests utilize isolated H2 database profiles (`test`) and MockMvc for reproducible CI/CD execution.
