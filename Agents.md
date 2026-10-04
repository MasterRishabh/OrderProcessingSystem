# Agents.md — Developer & AI Coding Agent Guidelines

This document provides definitive instructions, architectural principles, coding conventions, and operational workflows for engineers and AI coding agents working on the **Order Processing System**.

---

## 1. Project Structure

The project is structured as a Maven multi-module microservice repository:

```text
OrderSystem/
├── pom.xml                                 # Root Maven parent aggregator & dependency management
├── README.md                               # Quickstart guide & cURL examples
├── Architecture.md                         # Architecture, sequence diagrams & design decisions
├── SwaggerTestingJourney.md               # Step-by-step Swagger UI testing guide (view order to track order)
├── Agents.md                               # This document: agent & engineer development guidelines
├── AIAssistanceReport.md                   # AI usage report, issues found, and corrections made
├── common/                                 # Shared contracts, DTOs, Enums, and Kafka Event payloads
│   └── src/main/java/com/ecommerce/ordersystem/common/
│       ├── dto/                            # ItemDto, OrderCreateRequest, OrderResponse, ApiResponse, etc.
│       ├── enums/                          # OrderStatus, OrderItemStatus, PaymentStatus
│       └── event/                          # OrderCreatedEvent, OrderCancelledEvent, etc.
├── inventory-service/                      # Microservice on port 8081 (Item Catalog & Stock Management)
│   └── src/main/java/com/ecommerce/ordersystem/inventory/
│       ├── config/                         # DataInitializer (seeds sample items)
│       ├── controller/                     # ItemController (REST endpoints)
│       ├── exception/                      # InsufficientInventoryException, ItemNotFoundException
│       ├── model/                          # Item entity with @Version
│       ├── repository/                     # ItemRepository (atomic updates & pessimistic locking)
│       └── service/                        # InventoryService (reserve, release, deadlock prevention)
├── payment-service/                        # Microservice on port 8082 (Payment Adapter & Simulator)
│   └── src/main/java/com/ecommerce/ordersystem/payment/
│       ├── controller/                     # PaymentController (process, refund, runtime config)
│       ├── gateway/                        # PaymentGateway interface, config, and Simulated Adapter
│       ├── model/                          # PaymentTransaction entity
│       ├── repository/                     # PaymentTransactionRepository
│       └── service/                        # PaymentService (idempotency & config management)
└── order-service/                          # Microservice on port 8080 (Core Order Processing Engine)
    └── src/main/java/com/ecommerce/ordersystem/order/
        ├── client/                         # InventoryClient & PaymentClient (Resilience4j Circuit Breaker)
        ├── config/                         # RestClientConfig
        ├── controller/                     # OrderController (create, get, list, cancel, partial-cancel)
        ├── event/                          # OrderEventPublisher & OrderEventPublisherImpl
        ├── exception/                      # OrderNotFoundException, InvalidOrderStateException, etc.
        ├── model/                          # Order & OrderItem entities with lifecycle tracking
        ├── repository/                     # OrderRepository & OrderItemRepository
        ├── scheduler/                      # OrderStatusScheduler (5-minute background transition job)
        ├── service/                        # OrderService (orchestration facade & compensation logic)
        └── state/                          # OrderStateMachine (state transition validation)
```

---

## 2. Core Build & Test Commands

All commands should be executed from the repository root:

- **Full build & run all unit/integration tests**:
  ```powershell
  mvn clean test
  ```
- **Install all modules into local repository**:
  ```powershell
  mvn clean install
  ```
- **Run tests for an individual service**:
  ```powershell
  mvn test -pl order-service
  mvn test -pl inventory-service
  mvn test -pl payment-service
  ```
- **Skip tests during quick local packaging**:
  ```powershell
  mvn package -DskipTests
  ```

---

## 3. How to Run Locally

Each microservice is an independent Spring Boot application. They can be launched simultaneously in separate terminal windows:

### Terminal 1: Inventory Service (Port 8081)
```powershell
cd inventory-service
mvn spring-boot:run
```

### Terminal 2: Payment Service (Port 8082)
```powershell
cd payment-service
mvn spring-boot:run
```

### Terminal 3: Order Service (Port 8080)
```powershell
cd order-service
mvn spring-boot:run
```

Alternatively, run the root PowerShell script `.\run-services.ps1` to launch all three services.

---

## 4. Architectural Rules & Implementation Constraints

1. **Strict Problem Statement Compliance**:
   - Never invent arbitrary business rules when the problem statement already specifies them.
   - **Cancellation Constraint**: An order can be cancelled (full or partial) **only if it is in `PENDING` status**. Do not allow cancellation in `PROCESSING`, `SHIPPED`, or `DELIVERED`.
   - **Background Job**: The background transition job must execute every 5 minutes (`order.status-transition.fixed-delay-ms=300000`) to advance `PENDING` orders to `PROCESSING`.
2. **Zero-Docker Default Execution**:
   - The application must always remain capable of running out-of-the-box on local developer machines without requiring Docker or external installations.
   - H2 in PostgreSQL compatibility mode is the active default profile (`application.yml`).
   - PostgreSQL is supported via the `postgres` Spring profile (`application-postgres.yml`).
3. **Kafka Graceful Degradation**:
   - `app.kafka.enabled` defaults to `false`. When disabled, the `OrderEventPublisher` logs and dispatches events via Spring `ApplicationEventPublisher`. When enabled, it serializes JSON events to Kafka topics keyed by `orderId`.
4. **Idempotency & Concurrency**:
   - In any payment or order mutation, always accept an optional `Idempotency-Key` HTTP header.
   - Always sort multi-item inventory reservations by `codeId` to prevent database deadlocks.
   - Inventory decrements must be atomic (`UPDATE items SET quantity = quantity - :qty WHERE code_id = :codeId AND quantity >= :qty`).
5. **No Lombok Dependency**:
   - Use standard clean Java POJOs, constructors, getters, and setters.
   - Avoid Lombok to maintain 100% compatibility across all modern JDK releases (Java 17, 21, 25+) without annotation processing compiler crashes.
6. **Compiler Flag `-parameters`**:
   - The root POM configures `<parameters>true</parameters>` in `maven-compiler-plugin`. When adding Spring MVC `@PathVariable` or `@RequestParam`, always specify the parameter name explicitly: `@PathVariable("orderId") String orderId`.

---

## 5. Adding New Features

1. **Updating Contracts**:
   - Add new DTOs or Events into the `common` module first.
   - Run `mvn install -pl common` to make the new classes available to child modules.
2. **Database Migrations**:
   - Use JPA `@PrePersist` and `@PreUpdate` for audit timestamps.
   - If introducing schema migration scripts (e.g., Flyway/Liquibase), maintain dual compatibility for both H2 PostgreSQL-mode and PostgreSQL.
3. **Observability**:
   - All REST controllers must wrap responses in `ApiResponse<T>`.
   - All exceptions must be caught by `@RestControllerAdvice` and transformed into `ErrorResponse`.
   - Log at `INFO` level for business events and `WARN`/`ERROR` for failures with correlation IDs.

---

## 6. Testing Expectations

1. **Coverage**:
   - Every service must have JUnit 5 unit tests verifying core business logic with Mockito.
   - Every service must have `@SpringBootTest` + `MockMvc` integration tests verifying REST endpoints.
2. **Byte Buddy Experimental Flag**:
   - The root POM configures `-Dnet.bytebuddy.experimental=true` under `maven-surefire-plugin` to ensure Mockito bytecode enhancement functions seamlessly under latest JDKs. Keep this configuration intact.
