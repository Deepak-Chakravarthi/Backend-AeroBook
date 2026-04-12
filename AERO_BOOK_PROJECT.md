
✈️ AeroBook — Airline Reservation & Operations Platform


A production-grade Spring Boot backend application simulating a real-world airline
reservation system. Built with a focus on clean architecture, progressive complexity,
and showcasing industry-standard backend engineering concepts — starting from a
structured monolith and evolving toward a microservices-ready architecture.

---

## 📌 Project Goal

The primary goal of AeroBook is not just to build a working airline booking system,
but to demonstrate and implement every major backend engineering concept in a
real-world domain context — with proper design, architecture, and production-level standards.

---


## 🏗️ Architecture Philosophy

### Phase 1 — Structured Monolith (Current)
AeroBook starts as a well-structured, layer-based monolith with strict boundaries
between modules. Each module communicates through service interfaces, not direct
repository access across boundaries. This discipline makes the future split into
microservices a clean infrastructure exercise rather than a code rewrite.
Controller → Service → Repository → Database
↓
Domain Events (Spring ApplicationEventPublisher)
↓
Listeners (Notification, Loyalty side-effects)

### Phase 2 — Distributed Infrastructure (Planned)
Add Redis caching, Kafka event streaming, async processing, and rate limiting
while keeping the monolith intact.

### Phase 3 — Event Sourcing on Booking (Planned)
Introduce event sourcing on the Booking aggregate — the most audit-sensitive
module in the system.

### Phase 4 — Microservices Split (Planned)
Split along bounded contexts using the module boundaries already established
in Phase 1.

---

## 🧱 Tech Stack

| Layer              | Technology                                      |
|--------------------|-------------------------------------------------|
| Framework          | Spring Boot 3.5.11                              |
| Language           | Java 21                                         |
| Security           | Spring Security 7 + JWT (JJWT 0.12.6)          |
| Database           | PostgreSQL                                       |
| ORM                | Spring Data JPA + Hibernate                     |
| Migrations         | Flyway                                           |
| Mapping            | MapStruct                                        |
| Dynamic Queries    | JPA Specifications                               |
| Boilerplate        | Lombok                                           |
| API Docs           | SpringDoc OpenAPI 3 / Swagger UI                |
| Build Tool         | Maven                                            |
| Containerization   | Docker + Docker Compose (planned)               |

---