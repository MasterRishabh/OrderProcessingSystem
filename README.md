# E-Commerce Order Processing System

A production-quality, distributed **Order Processing Backend System** built with **Java 21**, **Spring Boot 3.3.4**, **Spring Data JPA**, **Resilience4j**, and **Apache Kafka**.

Designed for high concurrency, fault tolerance, transaction consistency, and strict compliance with the order processing lifecycle requirements.

---

## 1. Project Overview & Architecture Summary

The system is decomposed into three focused microservices with clear bounded contexts:

- **Order Service (`port: 8080`)**:
  - Manages order creation with multiple items.
  - Retrieves order details and filters orders by status with pagination.
  - Enforces strict cancellation rules: **orders can ONLY be cancelled while in `PENDING` status**.
  - Supports both **Full Cancellation** and **Partial Item Cancellation** (restoring stock and refunding amounts).
  - Contains an automated background job (`@Scheduled`) running **every 5 minutes** that advances `PENDING` orders to `PROCESSING`.
  - Employs a Resilience4j Circuit Breaker when communicating with the Payment Service.
  - Publishes lifecycle events to Kafka (`orders.created`, `orders.confirmed`, `orders.cancelled`, `orders.partially-cancelled`).
- **Inventory Service (`port: 8081`)**:
  - Maintains item catalog and stock quantities (`codeId`, `name`, `price`, `quantity`, `version`).
  - Protects against overselling and race conditions using atomic database queries (`UPDATE items SET quantity = quantity - :qty ... WHERE quantity >= :qty`).
  - Releases reserved quantities immediately if payment fails or an order is cancelled.
  - Deterministically sorts items by `codeId` to prevent database deadlocks across concurrent multi-item orders.
- **Payment Service (`port: 8082`)**:
  - Implements the **Adapter Pattern** with a `PaymentGateway` abstraction and a `SimulatedPaymentGatewayAdapter`.
  - Supports **dynamic runtime reconfiguration** of payment outcome (`SUCCESS` or `FAILURE`) and simulated latency via REST API **without requiring application restarts or code changes**.
  - Enforces idempotency via `idempotency_key` unique constraints.

---

## 2. Technology Stack

- **Language**: Java 21+ (tested on Java 21 and Oracle JDK 25)
- **Framework**: Spring Boot 3.3.4
- **Persistence**: Spring Data JPA / Hibernate ORM
- **Database**:
  - Embedded H2 in PostgreSQL compatibility mode (default out-of-the-box profile, zero-docker required)
  - PostgreSQL (supported via `postgres` profile)
- **Fault Tolerance**: Resilience4j Circuit Breaker (Spring Boot 3 starter)
- **Messaging**: Apache Kafka (`spring-kafka`) with in-memory Spring `ApplicationEventPublisher` fallback
- **Documentation**: SpringDoc OpenAPI / Swagger UI (v3)
- **Testing**: JUnit 5, Mockito, Spring Boot Test, MockMvc

---

## 3. Prerequisites & Quickstart

### Prerequisites
- JDK 21 or higher installed (`java -version`)
- Apache Maven 3.8+ installed (`mvn -version`)

### Build & Run Tests
To build all modules and run the full test suite (29 unit & integration tests):
```powershell
mvn clean install
```

---

## 4. How to Run Locally

You can launch the three microservices in separate terminal windows:

### Step 1: Start Inventory Service (Port 8081)
```powershell
cd inventory-service
mvn spring-boot:run
```
*Swagger UI*: [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)  
*Initial Seed Data*: Automatically seeds `ITEM-101` (MacBook), `ITEM-102` (Headphones), `ITEM-103` (Keyboard), `ITEM-104` (Monitor), and `ITEM-SINGLE` (Limited item with quantity 1).

### Step 2: Start Payment Service (Port 8082)
```powershell
cd payment-service
mvn spring-boot:run
```
*Swagger UI*: [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html)  
*Default Behavior*: Configured to return `SUCCESS` for payments.

### Step 3: Start Order Service (Port 8080)
```powershell
cd order-service
mvn spring-boot:run
```
*Swagger UI*: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### Optional: Running with External PostgreSQL
By default, the services run on in-memory H2 (PostgreSQL compatibility mode). If you want to connect to an external PostgreSQL instance:
1. Run `init-postgres.sql` to initialize `order_db`, `inventory_db`, and `payment_db` with `admin` / `admin` credentials:
   ```bash
   psql -U admin -d postgres -f init-postgres.sql
   ```
2. Start each service with the `postgres` Spring profile:
   ```powershell
   mvn spring-boot:run -Dspring-boot.run.profiles=postgres
   ```
   *(Default credentials configured: `admin` / `admin` on `localhost:5432`)*

---

## 4.1 Interactive Testing Journey (Swagger UI)
For a complete, interactive walkthrough with pre-filled working payloads:
👉 **[Read the Swagger Testing Journey Guide (View Order to Track Order)](file:///d:/interviews/OrderSystem/SwaggerTestingJourney.md)**

Every endpoint in Swagger UI includes working inputs for seamless 1-click execution:
- **Step 1**: View all orders (`GET /api/orders`)
- **Step 2**: Track order details by ID (`GET /api/orders/ORD-DEMO-001`)
- **Step 3**: Create a new order (`POST /api/orders`)
- **Step 4**: Advance & track status transitions (`PUT /api/orders/{orderId}/status`)
- **Step 5**: Test full or partial cancellation (`POST /api/orders/.../cancel`)

---

## 5. End-to-End Walkthrough with cURL Examples

### 1. View Inventory Items
```bash
curl -X GET "http://localhost:8081/api/items"
```

### 2. Create an Order (Multiple Items with Idempotency Key)
```bash
curl -X POST "http://localhost:8080/api/orders" \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: DEMO-ORDER-001" \
  -d '{
    "customerId": "CUST-ALICE",
    "items": [
      { "itemCodeId": "ITEM-101", "quantity": 1 },
      { "itemCodeId": "ITEM-102", "quantity": 2 }
    ],
    "paymentMethod": "CREDIT_CARD"
  }'
```
*Response*:
```json
{
  "success": true,
  "message": "Order created successfully",
  "data": {
    "id": "e43b1900-cf3b-4680-a65c-6b3252069b17",
    "customerId": "CUST-ALICE",
    "status": "PENDING",
    "totalAmount": 2399.97,
    "idempotencyKey": "DEMO-ORDER-001",
    "items": [
      {
        "id": "848c41f7-e730-4e50-93ee-8ec158f96e1b",
        "itemCodeId": "ITEM-101",
        "itemName": "MacBook Pro M3",
        "quantity": 1,
        "pricePerUnit": 1999.99,
        "subtotal": 1999.99,
        "status": "ACTIVE"
      },
      {
        "id": "fc18928a-6a56-4c4f-95fa-b6481ef0e851",
        "itemCodeId": "ITEM-102",
        "itemName": "Wireless Noise-Cancelling Headphones",
        "quantity": 2,
        "pricePerUnit": 199.99,
        "subtotal": 399.98,
        "status": "ACTIVE"
      }
    ]
  }
}
```

### 3. Retrieve Order Details by ID
```bash
curl -X GET "http://localhost:8080/api/orders/{orderId}"
```

### 4. List All Orders Filtered by Status
```bash
curl -X GET "http://localhost:8080/api/orders?status=PENDING&page=0&size=10"
```

### 5. Partial Order Cancellation (Cancel 1 item from the order)
> **Rule**: Allowed ONLY while the order is in `PENDING` status. Cancelled quantity is returned to inventory.
```bash
curl -X POST "http://localhost:8080/api/orders/{orderId}/items/{itemId}/cancel"
```
*Result*: Item status transitions to `CANCELLED`, order total amount is updated, refunded amount is logged, and stock is replenished in `Inventory Service`.

### 6. Full Order Cancellation
> **Rule**: Allowed ONLY while the order is in `PENDING` status.
```bash
curl -X POST "http://localhost:8080/api/orders/{orderId}/cancel"
```
*Result*: Order status becomes `CANCELLED`, all active items marked `CANCELLED`, all quantities restored to stock.

### 7. Verifying Cancellation Rejection when NOT in `PENDING`
Advance order status to `PROCESSING` or `SHIPPED`:
```bash
curl -X PUT "http://localhost:8080/api/orders/{orderId}/status" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "PROCESSING",
    "reason": "Warehouse preparing package"
  }'
```
Now attempt to cancel the order:
```bash
curl -X POST "http://localhost:8080/api/orders/{orderId}/cancel"
```
*Response*: HTTP `400 Bad Request`
```json
{
  "errorCode": "INVALID_ORDER_STATE",
  "message": "Cannot cancel order '{orderId}' in current status 'PROCESSING'",
  "details": ["Cannot cancel order '{orderId}' in current status 'PROCESSING'"]
}
```

### 8. Testing Payment Failure & Compensating Inventory Rollback
Change the simulated payment gateway behavior to `FAILURE` at runtime:
```bash
curl -X PUT "http://localhost:8082/api/payments/config" \
  -H "Content-Type: application/json" \
  -d '{
    "defaultResult": "FAILURE",
    "simulatedDelayMs": 0
  }'
```
Now place an order:
```bash
curl -X POST "http://localhost:8080/api/orders" \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST-BOB",
    "items": [
      { "itemCodeId": "ITEM-104", "quantity": 1 }
    ]
  }'
```
*Result*: Order is saved with status `PAYMENT_FAILED`, and the reserved inventory is **immediately released back to available stock** via a compensating rollback!

Reset payment configuration back to `SUCCESS`:
```bash
curl -X PUT "http://localhost:8082/api/payments/config" \
  -H "Content-Type: application/json" \
  -d '{
    "defaultResult": "SUCCESS",
    "simulatedDelayMs": 0
  }'
```

---

## 6. Background Scheduling Job

Problem statement requirement:
> *"A background job should automatically update PENDING orders to PROCESSING every 5 minutes."*

- Implemented in `OrderStatusScheduler.java` using Spring `@Scheduled`.
- Configured in `order-service/src/main/resources/application.yml`:
  ```yaml
  order:
    status-transition:
      scheduler:
        enabled: true
      fixed-delay-ms: 300000   # 5 minutes in milliseconds
      initial-delay-ms: 10000
  ```
- Every 5 minutes, it queries all orders with status `PENDING`, transitions them to `PROCESSING`, and records audit logs.

---

## 7. Additional Documentation

- [Architecture.md](file:///d:/interviews/OrderSystem/Architecture.md) — Architectural diagrams (Mermaid), sequence flows, state machine, and concurrency analysis.
- [Agents.md](file:///d:/interviews/OrderSystem/Agents.md) — Developer and AI agent coding guidelines, conventions, and operational constraints.
- [AIAssistanceReport.md](file:///d:/interviews/OrderSystem/AIAssistanceReport.md) — Comprehensive report detailing AI tooling usage, issues encountered, and corrections made.
