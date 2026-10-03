# PackFlow – Testing Strategy & Test Suite Documentation

## 1. Testing Philosophy & Scope

PackFlow employs a multi-tiered automated testing suite utilizing **JUnit 5 (Jupiter)**. The suite verifies pure business logic, mathematical precision, state machine validity, security primitives, and end-to-end multi-table database transactions.

---

## 2. Test Suite Overview

| Test Class | Focus Area | Nature | Passed / Total |
|---|---|---|---|
| `OrderCostCalculationTest` | Order and line item financial calculations, quantity rounding, zero/negative guards | Unit Test (Pure Java) | 4 / 4 |
| `OrderStatusTransitionTest` | Valid and illegal status progression rules according to state machine | Unit Test (State Pattern) | 3 / 3 |
| `InventoryStockLogicTest` | Availability balance math (`on_hand - reserved`), reorder threshold triggers | Unit Test (Pure Java) | 3 / 3 |
| `OrderWorkflowIntegrationTest` | Full lifecycle: Order creation -> Stock reservation -> Rollback on shortage -> Cancellation stock release | Integration Test (Live MySQL JDBC) | 2 / 2 |
| `DBConnectionTest` | Connection pooling, driver registration, database reachability | Integration Test (JDBC) | 1 / 1 |
| `PasswordUtilTest` | BCrypt salt generation, hashing consistency, valid vs invalid password validation | Unit / Security Test | 2 / 2 |
| **Total** | | | **15 / 15 (100% Pass)** |

---

## 3. Key Test Scenarios & Edge Cases

### 3.1 Order Financial Calculation Precision (`OrderCostCalculationTest`)
- **Objective**: Ensure that product unit cost and packaging unit cost are correctly summed and multiplied by quantity without floating-point distortion.
- **Assertions**:
  - `OrderItem` line total calculation: `(unitCost + packagingCost) * quantity`.
  - Multi-item aggregation into Order subtotal, packaging cost total, and grand total.
  - Verification that negative quantities are prohibited by domain invariants.

### 3.2 State Machine Enforcement (`OrderStatusTransitionTest`)
- **Objective**: Prevent out-of-order execution (e.g., jumping from `PENDING` directly to `DELIVERED`, or modifying a `CANCELLED` order).
- **Assertions**:
  - `PENDING.canTransitionTo(APPROVED) == true`
  - `PENDING.canTransitionTo(DELIVERED) == false`
  - `CANCELLED.canTransitionTo(...) == false` (terminal state)
  - `DELIVERED.canTransitionTo(...) == false` (terminal state)

### 3.3 Atomic Transaction & Stock Rollback (`OrderWorkflowIntegrationTest`)
- **Objective**: Verify that when an order approval demands more inventory than physically available, the system throws `InsufficientInventoryException` and performs an immediate transaction rollback.
- **Scenario**:
  1. Create a live order requesting 5,000,000 units of corrugated boxes (exceeding stock of 650 units).
  2. Attempt `orderService.approveOrderAndReserveInventory(orderId)`.
  3. Assert `InsufficientInventoryException` is thrown.
  4. Query the database to verify the order status remains `PENDING` and warehouse inventory numbers are **completely unchanged**.

### 3.4 Stock Reservation & Release on Cancellation
- **Objective**: Verify that reserved stock is cleanly returned to the available inventory pool when an approved order is cancelled.
- **Scenario**:
  1. Record initial available stock of item.
  2. Create and approve order with 10 units.
  3. Verify available stock decreased by 10 and reserved stock increased by 10.
  4. Cancel order.
  5. Verify available stock increased by 10 and reserved stock returned to original count.

---

## 4. Running the Tests

Execute all tests from the project root using Maven:

```bash
mvn test
```

To run a specific test class:

```bash
mvn test -Dtest=OrderWorkflowIntegrationTest
```

To execute a clean compilation and package:

```bash
mvn clean package
```
