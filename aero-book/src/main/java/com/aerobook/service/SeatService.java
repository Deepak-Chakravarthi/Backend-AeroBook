package com.aerobook.service;


import com.aerobook.entity.AircraftSeatConfig;
import com.aerobook.entity.Flight;
import com.aerobook.entity.Seat;
import com.aerobook.domain.dto.request.get.SeatGetRequest;
import com.aerobook.domain.dto.request.SeatHoldRequest;
import com.aerobook.domain.dto.request.SeatReleaseRequest;
import com.aerobook.domain.dto.response.SeatHoldResponse;
import com.aerobook.domain.dto.response.SeatResponse;
import com.aerobook.domain.enums.SeatClass;
import com.aerobook.domain.enums.SeatStatus;
import com.aerobook.domain.enums.SeatType;
import com.aerobook.exception.AeroBookException;
import com.aerobook.exception.ResourceNotFoundException;
import com.aerobook.mapper.SeatMapper;
import com.aerobook.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeatService {

    private final SeatRepository         seatRepository;
    private final SeatMapper             seatMapper;
    private final SeatInventoryService   inventoryService;
    private final FlightService          flightService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${aerobook.seat.hold-duration-minutes:15}")
    private int holdDurationMinutes;

    private static final String SEAT_HOLD_KEY = "seat:hold:%s:%s";  // flightId:bookingRef

    // ----------------------------------------------------------------
    // Get seats — filterable
    // ----------------------------------------------------------------
    public List<SeatResponse> getSeats(SeatGetRequest request, Pageable pageable) {
        return seatRepository.findAll(request.toSpecification(), pageable)
                .map(seatMapper::toSeatResponse)
                .getContent();
    }

    // ----------------------------------------------------------------
    // Generate seat map for a flight
    // Called when a flight is created — generates all physical seats
    // based on aircraft seat configuration
    // ----------------------------------------------------------------
    @Transactional
    public void generateSeatMap(Flight flight) {
        if (flight.getAircraft().getSeatConfigs() == null
                || flight.getAircraft().getSeatConfigs().isEmpty()) {
            log.warn("No seat configs found for aircraft {} — skipping seat map generation",
                    flight.getAircraft().getRegistrationNumber());
            return;
        }

        List<Seat> seats = new ArrayList<>();

        for (var config : flight.getAircraft().getSeatConfigs()) {
            seats.addAll(generateSeatsForClass(flight, config));
        }

        seatRepository.saveAll(seats);
        log.info("Generated {} seats for flight {}",
                seats.size(), flight.getFlightNumber());
    }

    @Transactional
    public SeatHoldResponse holdSeats(SeatHoldRequest request) {

        String lockKey = "lock:seat:hold:" + request.flightId();

        Boolean lockAcquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "LOCKED", 10, TimeUnit.SECONDS);

        if (Boolean.FALSE.equals(lockAcquired)) {
            throw new AeroBookException(
                    "Another booking is in progress. Please try again.",
                    HttpStatus.CONFLICT,
                    "SEAT_LOCKED"
            );
        }

        try {
            // 1. Fetch flight
            Flight flight = flightService.findFlightById(request.flightId());

            // 2. Generate booking ref early (needed for seat assignment)
            String bookingRef = generateBookingRef();

            LocalDateTime holdUntil = LocalDateTime.now()
                    .plusMinutes(holdDurationMinutes);

            // 3. Hold inventory (optimistic locking + retry)
            inventoryService.holdSeats(
                    request.flightId(),
                    request.seatClass(),
                    request.seatCount()
            );

            // 4. Allocate and HOLD physical seats (pessimistic lock)
            List<Seat> allocatedSeats = allocatePhysicalSeats(
                    request.flightId(),
                    request.seatClass(),
                    request.seatCount(),
                    request.preferredSeatNumber(),
                    bookingRef,
                    holdUntil
            );

            // 5. Store hold in Redis (TTL)
            storeHoldInRedis(
                    request.flightId(),
                    bookingRef,
                    request.seatClass(),
                    request.seatCount(),
                    holdUntil
            );

            // 6. Prepare response
            List<String> seatNumbers = allocatedSeats.stream()
                    .map(Seat::getSeatNumber)
                    .toList();

            int remainingSeats = inventoryService
                    .findInventory(request.flightId(), request.seatClass())
                    .getAvailableSeats();

            log.info("Held {} seats on flight {} class {} — ref: {} until {}",
                    request.seatCount(),
                    request.flightId(),
                    request.seatClass(),
                    bookingRef,
                    holdUntil);

            return new SeatHoldResponse(
                    bookingRef,
                    flight.getId(),
                    flight.getFlightNumber(),
                    request.seatClass(),
                    request.seatCount(),
                    seatNumbers,
                    holdUntil,
                    remainingSeats
            );

        } catch (Exception e) {
            inventoryService.releaseHeldSeats(
                    request.flightId(),
                    request.seatClass(),
                    request.seatCount()
            );
            throw e;

        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    @Transactional
    public void releaseSeats(SeatReleaseRequest request) {

        String lockKey = "lock:seat:release:" + request.bookingRef();

        Boolean lockAcquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "LOCKED", 10, TimeUnit.SECONDS);

        if (Boolean.FALSE.equals(lockAcquired)) {
            throw new AeroBookException(
                    "Release already in progress",
                    HttpStatus.CONFLICT,
                    "RELEASE_LOCKED"
            );
        }

        try {
            // 🔐 Fetch seats WITH DB lock
            List<Seat> heldSeats = seatRepository
                    .findAllByBookingRefForUpdate(request.bookingRef());

            if (heldSeats.isEmpty()) {
                log.warn("No held seats found for booking ref: {}", request.bookingRef());
                return;
            }

            // ✅ Idempotency check (VERY IMPORTANT)
            boolean alreadyReleased = heldSeats.stream()
                    .allMatch(seat -> seat.getStatus() == SeatStatus.AVAILABLE);

            if (alreadyReleased) {
                log.warn("Seats already released for booking ref: {}", request.bookingRef());
                return;
            }

            // 🔁 Group by class (same as your logic)
            Map<SeatClass, List<Seat>> groupedSeats = heldSeats.stream()
                    .collect(Collectors.groupingBy(Seat::getSeatClass));

            for (Map.Entry<SeatClass, List<Seat>> entry : groupedSeats.entrySet()) {

                SeatClass seatClass = entry.getKey();
                List<Seat> seats = entry.getValue();

                // 🔓 Release seats (atomic)
                seats.forEach(Seat::release);
                seatRepository.saveAll(seats);

                // ➕ Restore inventory
                inventoryService.releaseHeldSeats(
                        request.flightId(),
                        seatClass,
                        seats.size()
                );

                // ➖ Decrement Redis counter (if used in hold)
                decrementHoldCounter(request.flightId(), seatClass, seats.size());
            }

            // 🧹 Remove Redis hold
            removeHoldFromRedis(request.flightId(), request.bookingRef());

            log.info("Released {} seats for booking ref: {}",
                    heldSeats.size(), request.bookingRef());

        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    private void decrementHoldCounter(Long flightId,
                                      SeatClass seatClass,
                                      int count) {

        String key = "seat:hold:count:" + flightId + ":" + seatClass;

        Object value = redisTemplate.opsForValue().get(key);

        Long current = (value instanceof Number)
                ? ((Number) value).longValue()
                : null;

        if (current != null) {
            redisTemplate.opsForValue().decrement(key, count);
        }
    }

    // ----------------------------------------------------------------
    // Confirm seats on payment success
    // ----------------------------------------------------------------
    @Transactional
    public void confirmSeats(Long flightId, String bookingRef) {
        List<Seat> heldSeats = seatRepository.findAllByBookingRef(bookingRef);

        if (heldSeats.isEmpty()) {
            throw new AeroBookException(
                    "No held seats found for booking ref: " + bookingRef,
                    HttpStatus.NOT_FOUND,
                    "SEATS_NOT_FOUND"
            );
        }

        heldSeats.stream()
                .collect(java.util.stream.Collectors.groupingBy(Seat::getSeatClass))
                .forEach((seatClass, seats) -> {
                    seats.forEach(Seat::confirmBooking);
                    seatRepository.saveAll(seats);
                    inventoryService.confirmBooking(flightId, seatClass, seats.size());
                });

        removeHoldFromRedis(flightId, bookingRef);

        log.info("Confirmed seats for booking ref: {}", bookingRef);
    }

    // ----------------------------------------------------------------
    // Release expired holds — called by scheduler
    // ----------------------------------------------------------------
    @Transactional
    public void releaseExpiredHolds() {
        int released = seatRepository.releaseExpiredHolds(LocalDateTime.now());
        if (released > 0) {
            log.info("Released {} expired seat holds", released);
        }
    }

    @Transactional
    public Flight generateSeatMapForFlight(Long flightId) {
        Flight flight = flightService.findFlightById(flightId);
        generateSeatMap(flight);
        return flight;
    }

    // ----------------------------------------------------------------
    // Private helpers
    // ----------------------------------------------------------------

    private List<Seat> generateSeatsForClass(
            Flight flight,
            AircraftSeatConfig config) {

        List<Seat> seats = new ArrayList<>();
        String[] letters = {"A", "B", "C", "D", "E", "F"};
        int seatsPerRow   = config.getSeatsPerRow() != null
                ? config.getSeatsPerRow() : 6;

        for (int row = 1; row <= config.getRows(); row++) {
            for (int col = 0; col < seatsPerRow; col++) {
                String letter     = letters[col % letters.length];
                String seatNumber = row + letter;
                SeatType seatType = resolveSeatType(col, seatsPerRow);

                seats.add(Seat.builder()
                        .flight(flight)
                        .seatNumber(seatNumber)
                        .rowNumber(row)
                        .seatLetter(letter)
                        .seatClass(config.getSeatClass())
                        .seatType(seatType)
                        .status(SeatStatus.AVAILABLE)
                        .build());
            }
        }
        return seats;
    }

    private SeatType resolveSeatType(int colIndex, int seatsPerRow) {
        if (colIndex == 0 || colIndex == seatsPerRow - 1) return SeatType.WINDOW;
        if (colIndex == 1 || colIndex == seatsPerRow - 2) return SeatType.AISLE;
        return SeatType.MIDDLE;
    }

    private List<Seat> allocatePhysicalSeats(Long flightId,
                                             SeatClass seatClass,
                                             int count,
                                             String preferredSeat,
                                             String bookingRef,
                                             LocalDateTime holdUntil) {

        List<Seat> selectedSeats;

        // 1. Preferred seat flow
        if (preferredSeat != null) {

            Seat seat = seatRepository.findBySeatNumberForUpdate(flightId, preferredSeat)
                    .orElseThrow(() -> new AeroBookException(
                            "Preferred seat not found",
                            HttpStatus.NOT_FOUND,
                            "SEAT_NOT_FOUND"
                    ));

            if (seat.getStatus() != SeatStatus.AVAILABLE) {
                throw new AeroBookException(
                        "Preferred seat not available",
                        HttpStatus.CONFLICT,
                        "SEAT_NOT_AVAILABLE"
                );
            }

            selectedSeats = List.of(seat);

        } else {
            // 2. Auto allocation with DB lock
            List<Seat> available = seatRepository
                    .findAvailableSeatsForUpdate(flightId, seatClass);

            if (available.size() < count) {
                throw new AeroBookException(
                        "Insufficient physical seats available. Requested: "
                                + count + ", Available: " + available.size(),
                        HttpStatus.CONFLICT,
                        "INSUFFICIENT_SEATS"
                );
            }

            selectedSeats = available.subList(0, count);
        }

        // 3. Atomic seat update (CRITICAL)
        for (Seat seat : selectedSeats) {
            seat.setStatus(SeatStatus.HELD);
            seat.setHeldByBookingRef(bookingRef);
            seat.setHeldUntil(holdUntil);
        }

        return seatRepository.saveAll(selectedSeats);
    }

    private List<Seat> allocatePreferredSeat(Long flightId,
                                             String seatNumber, int count) {
        Seat seat = seatRepository.findByFlightIdAndSeatNumber(flightId, seatNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Seat", "seatNumber", seatNumber));

        if (seat.getStatus() != SeatStatus.AVAILABLE) {
            throw new AeroBookException(
                    "Preferred seat " + seatNumber + " is not available",
                    HttpStatus.CONFLICT,
                    "PREFERRED_SEAT_UNAVAILABLE"
            );
        }

        return List.of(seat);
    }

    private void validateNoActiveHold(Long flightId, SeatClass seatClass) {
        String pattern = String.format("seat:hold:%d:*", flightId);
        var keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            log.debug("Active holds found in Redis for flight {} — proceeding with inventory check",
                    flightId);
        }
    }

    private void storeHoldInRedis(Long flightId,
                                  String bookingRef,
                                  SeatClass seatClass,
                                  int count,
                                  LocalDateTime holdUntil) {

        String key = String.format("seat:hold:%d:%s", flightId, bookingRef);

        Map<String, Object> holdData = Map.of(
                "flightId", flightId,
                "bookingRef", bookingRef,
                "seatClass", seatClass.name(),
                "count", count,
                "holdUntil", holdUntil.toString()
        );

        redisTemplate.opsForHash().putAll(key, holdData);

        // TTL
        redisTemplate.expire(key, holdDurationMinutes, TimeUnit.MINUTES);

        // ✅ Optional: hold counter (better tracking)
        String counterKey = "seat:hold:count:" + flightId + ":" + seatClass;

        redisTemplate.opsForValue().increment(counterKey, count);
        redisTemplate.expire(counterKey, holdDurationMinutes, TimeUnit.MINUTES);

        log.debug("Stored hold in Redis: key={}, TTL={}min", key, holdDurationMinutes);
    }

    private void removeHoldFromRedis(Long flightId, String bookingRef) {

        String key = String.format(SEAT_HOLD_KEY, flightId, bookingRef);

        Boolean deleted = redisTemplate.delete(key);

        if (Boolean.TRUE.equals(deleted)) {
            log.debug("Removed seat hold from Redis: key={}", key);
        } else {
            log.warn("Redis hold key not found or already deleted: key={}", key);
        }
    }
    private String generateBookingRef() {
        return "HOLD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}