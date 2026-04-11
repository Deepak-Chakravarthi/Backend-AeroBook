
## 🔍 Dynamic Query Architecture

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
