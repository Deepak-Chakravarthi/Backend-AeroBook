## Caching Strategy

The application uses a hybrid caching approach combining in-memory caching and distributed caching to achieve high performance and scalability.

1. In-Memory Cache (Reference Data)
2. Reference data modules (like Airline, Aircraft, Airport, Route) are cached in-memory at application startup using structures backed by Caffeine (ConcurrentHashMap-based).
3. This eliminates repeated database calls for static data and enables O(1) lookups.
4. Startup Cache Loading
5. Data is preloaded using @PostConstruct, ensuring all reference data is readily available as soon as the application starts.
6. Distributed Cache with Redis
7. For dynamic and high-traffic modules like Flight Search, Schedules, Seat Availability, and Inventory, Redis is used.
8. This allows sharing cached data across multiple instances and improves response time for frequently accessed queries.
9. TTL & Cache Eviction
10. Redis caches are configured with TTL (Time-To-Live) to ensure stale data is automatically expired. Cache eviction is triggered on updates (e.g., flight updates invalidate search cache).


## Example Usage
1. In-Memory Cache (Reference Data)

   ```@Component
   public class AircraftCacheStore {

   private final Map<Long, Aircraft> byId = new ConcurrentHashMap<>();

   @PostConstruct
   public void loadCache() {
   List<Aircraft> aircraftList = aircraftRepository.findAll();
   aircraftList.forEach(a -> byId.put(a.getId(), a));
   }

   public Aircraft getById(Long id) {
   return byId.get(id); // O(1) lookup
   }

   public void add(Aircraft aircraft) {
   byId.put(aircraft.getId(), aircraft); // keep cache in sync
   }
   }
   ```
2. Redis Cache (Flight Search)

   ```@Cacheable(value = "flight-search", key = "#request.cacheKey()")
   public List<FlightResponse> searchFlights(FlightSearchRequest request) {
   return flightSearchRepository.search(request);
   }
   @CacheEvict(value = "flight-search", allEntries = true)
   public void updateFlight(Long flightId, FlightUpdateRequest request) {
   // update flight logic
   }```
   
## Key Benefits
1.    Ultra-fast lookups for reference data (in-memory)
2.    Scalable caching across instances using Redis
3.    Reduced DB load and improved response times
4.    Automatic cache consistency with eviction strategies