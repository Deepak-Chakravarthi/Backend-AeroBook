
-----------------------Phase-1 Module summary ------------------------------

### Module 1 — Security & Auth
* JWT-based authentication with Spring Security (stateless sessions, BCrypt, role-based access via @PreAuthorize)
* Custom auth pipeline (JwtAuthenticationFilter, EntryPoint, UserDetailsService, DaoAuthenticationProvider) for secure request handling
* AOP-based authorization control with annotations (@ExemptAuthorization, @AuthenticatedEndpoint) and strict default-deny policy

### Module 2 — User & Profile
* User & Role management system (many-to-many roles, status lifecycle, BCrypt-secured credentials)
* Authentication & user operations via AuthService/UserService (register, login, CRUD, role assignment, profile management)
* Dynamic querying & strict access control (JpaSpecification filtering, SUPER_ADMIN privileges, validation rules, Flyway-seeded data)


### Module 3 — Airline & Aircraft (Reference Data)
* Airline & aircraft reference data model (seat configurations, enums, validations)
* High-performance in-memory caching with caffeine
* Flexible search & optimized queries (JpaSpecification filters, JOIN FETCH for seat configs, lightweight response mapping)


### Module 4 — Airport & Route (Reference Data)
* Airport & route domain modeling (IATA-based relationships, route validation, unique constraints)
* Advanced querying with cross-entity filtering (JpaSpecification, JOIN on airports, JPQL for route lookups)
* High-performance in-memory caching with caffeine
* Optimized data fetching & mapping (JOIN FETCH for origin/destination, embedded responses, reusable service helpers)


### Module 5 — Flight & Schedule
* Flight domain with schedules & fares (recurring schedules, seat-class pricing, validations)
* Automated flight generation (schedule-based creation, duplicate prevention, day mapping, bulk generation APIs)
* Optimized search & lifecycle handling (JOIN FETCH queries, status events, Redis cache eviction, dependency-safe design)

### Module 6 — Seat & Inventory
- Optimistic locking (`@Version`) for concurrent seat booking
- Seat locking with TTL using Redis
- Seat availability tracking per flight per class

### Module 7 — Flight Search
- Elasticsearch-backed search read model (CQRS read side)
- Redis caching for search results with TTL
- One-way and return flight search

### Module 8 — Booking
- PNR generation (unique booking reference)
- Saga pattern for distributed booking transaction
- Transactional Outbox pattern for reliable event publishing
- Booking lifecycle state machine

### Module 9 — Passenger & Ticket
- Multi-passenger bookings
- Ticket generation per passenger per flight
- Passenger linked optionally to system User

### Module 10 — Payment
- Idempotency keys on all payment APIs
- Payment status lifecycle (INITIATED → SUCCESS → REFUNDED)
- Refund processing

### Module 11 — Check-in & Boarding Pass
- Web check-in window enforcement (24h before departure)
- Seat selection during check-in
- PDF boarding pass generation

### Module 12 — Loyalty & Miles
- Frequent flyer account per user
- Miles earned per flight completion
- Tier progression (BLUE → SILVER → GOLD → PLATINUM)
- Event-driven — triggered by booking/flight completion events

### Module 13 — Notifications
- Spring ApplicationEventPublisher for internal events
- Kafka for cross-service events (Phase 2+)
- Email/SMS via async consumers
- Dead letter queue handling

### Module 14 — Admin Operations
- Flight delay/cancellation with cascading notifications
- Bulk flight management
- Revenue and occupancy reports