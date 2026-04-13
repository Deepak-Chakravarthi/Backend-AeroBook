# ✈️ AeroBook – Seat Hold & Release Module
---

## 📌 Overview

This module implements a **highly concurrent, production-grade seat reservation system** for airline booking.

It ensures:

* ✅ No double booking
* ✅ Strong consistency
* ✅ Scalable distributed locking
* ✅ Temporary holds with expiry

---

## 🧱 Architecture

### 🔹 Dual-Layer Design

```text
          +----------------------+
          |   SeatInventory      |
          | (Logical - Count)    |
          +----------+-----------+
                     |
                     ↓
          +----------------------+
          |        Seat          |
          | (Physical - Actual)  |
          +----------------------+
```

| Layer             | Responsibility                         |
| ----------------- | -------------------------------------- |
| **SeatInventory** | Tracks available seats count           |
| **Seat**          | Stores actual seat numbers (1A, 1B...) |

---

## 🔁 Hold Seat Flow

### 📡 Endpoint

```
POST /hold
```

### 🔄 Sequence Diagram

```text
User
  │
  ▼
[Controller]
  │
  ▼
[Redis Lock] ──────── (Prevent parallel requests)
  │
  ▼
[Inventory Service] ─ (Optimistic Locking)
  │
  ▼
[Seat Repository] ─── (Pessimistic Locking)
  │
  ▼
[Seat Update → HELD]
  │
  ▼
[Redis Store (TTL)]
  │
  ▼
Response (Booking Ref + Seats)
```

---

## 🔓 Release Seat Flow

### 📡 Endpoint

```
POST /release
```

### 🔄 Sequence Diagram

```text
User
  │
  ▼
[Controller]
  │
  ▼
[Redis Lock]
  │
  ▼
[Seat Fetch (FOR UPDATE)]
  │
  ▼
[Idempotency Check]
  │
  ▼
[Seat Update → AVAILABLE]
  │
  ▼
[Inventory Restore]
  │
  ▼
[Redis Cleanup]
```

---

## 🔐 Concurrency Strategy

### ✅ 1. Optimistic Locking (Inventory)

* Prevents overselling
* Retries on conflict

```java
@Version
private Long version;
```

---

### ✅ 2. Pessimistic Locking (Seats)

* Ensures no duplicate seat allocation

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
```

---

### ✅ 3. Redis Distributed Lock

* Prevents parallel execution across instances

```text
lock:seat:hold:{flightId}
lock:seat:release:{bookingRef}
```

---

## 🧠 Redis Design

### 🔑 Keys

```text
seat:hold:{flightId}:{bookingRef}
seat:hold:count:{flightId}:{seatClass}
lock:seat:hold:{flightId}
```

---

### ⏳ TTL

* Automatically expires holds
* Prevents stale reservations

---

## ⚙️ Setup

### 🐳 Run Redis

```bash
docker run -d -p 6379:6379 --name redis redis
```

---

### 🔧 Spring Boot Config

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
```

---

## 🧪 Example Flow

### Hold Request

```json
{
  "flightId": 101,
  "seatClass": "ECONOMY",
  "seatCount": 2
}
```

---

### Response

```json
{
  "bookingRef": "HOLD-AB12CD34",
  "seatNumbers": ["12A", "12B"],
  "holdUntil": "2026-04-03T22:30:00"
}
```

---

## ⚠️ Failure Handling

| Scenario                | Handling           |
| ----------------------- | ------------------ |
| Concurrent booking      | Redis + DB locks   |
| Inventory conflict      | Retry (optimistic) |
| Seat allocation failure | Rollback inventory |
| Duplicate release       | Idempotent check   |
| Redis failure           | Optional fallback  |

---

## 🔄 Scheduler (Auto Cleanup)

```text
Expired Hold
   ↓
Find Seats (HELD & expired)
   ↓
Release Seats
   ↓
Restore Inventory
```

---

## 🚀 Advantages

* 🔒 Strong consistency
* ⚡ High performance
* 🌐 Distributed safe
* 🧠 Real-world booking design
* 🔁 Idempotent operations

---

## 💬 Summary

This module demonstrates a **real-world airline booking strategy** combining:

* **Optimistic locking** → inventory safety
* **Pessimistic locking** → seat-level consistency
* **Redis** → distributed coordination & TTL

---

