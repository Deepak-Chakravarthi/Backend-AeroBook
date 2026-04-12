## Database Design

Database Design

The project uses a robust and production-grade database design leveraging PostgreSQL along with Spring Data JPA 
to ensure scalability, performance, and maintainability.

1. PostgreSQL as Primary Database
2. Chosen for its reliability, ACID compliance, and strong support for complex relational data models, making it ideal for enterprise-grade applications.
3. Reduced Boilerplate with Spring Data JPA
4. Spring Data JPA minimizes configuration and setup effort by providing ready-to-use repository abstractions.
5. Default JPA Methods for Simple Operations
6. Straightforward database operations are handled using built-in methods like save(), findById(), and delete().
7. Optimized Queries with JPQL & Native SQL
8. Complex joins and performance-critical operations are implemented using JPQL and native queries for better control and efficiency.
9. Dynamic Queries using JPA Specifications
10. JPA Specifications are used to construct flexible and dynamic queries at runtime, especially for search/filter APIs.
11. Pagination is handled with Custom pagination options


## Example Usage

**1. Simple CRUD using JPA Repository**

   `public interface UserRepository extends JpaRepository<User, Long> {
   }
   User user = userRepository.findById(id)
   .orElseThrow(() -> new NotFoundException("User not found"));

   userRepository.save(user);`

**2. JPQL Query for Complex Join**

  ` @Query("""
   SELECT f FROM Flight f
   JOIN FETCH f.airline a
   JOIN FETCH f.route r
   WHERE a.id = :airlineId
   """)
   List<Flight> findFlightsByAirline(@Param("airlineId") Long airlineId);`

**3. Native Query (Performance Critical)**

`   @Query(value = """
   SELECT * FROM flights f
   WHERE f.departure_date = :date
   AND f.status = 'SCHEDULED'
   """, nativeQuery = true)
   List<Flight> findScheduledFlights(@Param("date") LocalDate date);`

**4.Dynamic Query Architecture**

Every GET endpoint supports multi-criteria filtering via JPA Specifications.
The `SpecificationBuilder` utility is used inside each request object's
`toSpecification()` method — keeping the service layer completely clean.
```java
// Request object owns its own query logic
public Specification<Flight> toSpecification() {
    return SpecificationBuilder.<Flight>builder()
            .addEquals("flightNumber", flightNumber)
            .addJoinEquals("route.origin", "iataCode", originCode)
            .addEnumEquals("status", status, FlightStatus.class)
            .build();
}

// Service is one line
public List<FlightResponse> getFlights(FlightGetRequest request, Pageable pageable) {
    return flightRepository.findAll(request.toSpecification(), pageable)
            .map(flightMapper::toResponse)
            .getContent();
}
```

### Pagination
All GET endpoints support pagination via custom `PageableResolver`:
?page=0&size=20&sortBy=departureTime&sortDirection=ASC

Defaults: `page=0`, `size=100`, `sort=id ASC`

---
## Key Benefits
1. Production-grade reliability with PostgreSQL
2. Clean and minimal repository layer
3. Optimized query performance using JPQL/native SQL
4. Flexible runtime filtering with Specifications