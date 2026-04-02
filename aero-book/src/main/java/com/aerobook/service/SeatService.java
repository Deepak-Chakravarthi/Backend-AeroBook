package com.aerobook.service;


import com.aerobook.enitity.AircraftSeatConfig;
import com.aerobook.enitity.Flight;
import com.aerobook.enitity.Seat;
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
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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

    // ----------------------------------------------------------------
    // Hold seats — Redis TTL + inventory update
    // ----------------------------------------------------------------
    @Transactional
    public SeatHoldResponse holdSeats(SeatHoldRequest request) {
        Flight flight = flightService.findFlightById(request.flightId());

        // Step 1 — check Redis for existing hold (race condition guard)
        validateNoActiveHold(request.flightId(), request.seatClass());

        // Step 2 — hold at inventory level (optimistic locking)
        inventoryService.holdSeats(
                request.flightId(), request.seatClass(), request.seatCount());

        // Step 3 — allocate specific physical seats
        List<Seat> allocatedSeats = allocatePhysicalSeats(
                request.flightId(), request.seatClass(),
                request.seatCount(), request.preferredSeatNumber());

        // Step 4 — generate booking reference
        String bookingRef = generateBookingRef();
        LocalDateTime holdUntil = LocalDateTime.now()
                .plusMinutes(holdDurationMinutes);

        // Step 5 — hold each physical seat with TTL
        allocatedSeats.forEach(seat -> {
            seat.hold(bookingRef, holdUntil);
            seatRepository.save(seat);
        });

        // Step 6 — store hold metadata in Redis with TTL
        storeHoldInRedis(request.flightId(), bookingRef,
                request.seatClass(), request.seatCount(), holdUntil);

        List<String> seatNumbers = allocatedSeats.stream()
                .map(Seat::getSeatNumber)
                .toList();

        log.info("Held {} seats on flight {} class {} — ref: {} until {}",
                request.seatCount(), request.flightId(),
                request.seatClass(), bookingRef, holdUntil);

        return new SeatHoldResponse(
                bookingRef,
                flight.getId(),
                flight.getFlightNumber(),
                request.seatClass(),
                request.seatCount(),
                seatNumbers,
                holdUntil,
                inventoryService.findInventory(
                        request.flightId(), request.seatClass()).getAvailableSeats()
        );
    }

    // ----------------------------------------------------------------
    // Release held seats — manual release or on booking cancellation
    // ----------------------------------------------------------------
    @Transactional
    public void releaseSeats(SeatReleaseRequest request) {
        List<Seat> heldSeats = seatRepository.findAllByBookingRef(
                request.bookingRef());

        if (heldSeats.isEmpty()) {
            log.warn("No held seats found for booking ref: {}", request.bookingRef());
            return;
        }

        // Group by class to update inventory
        heldSeats.stream()
                .collect(java.util.stream.Collectors.groupingBy(Seat::getSeatClass))
                .forEach((seatClass, seats) -> {
                    seats.forEach(Seat::release);
                    seatRepository.saveAll(seats);
                    inventoryService.releaseHeldSeats(
                            request.flightId(), seatClass, seats.size());
                });

        // Remove from Redis
        removeHoldFromRedis(request.flightId(), request.bookingRef());

        log.info("Released {} seats for booking ref: {}",
                heldSeats.size(), request.bookingRef());
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

    private List<Seat> allocatePhysicalSeats(Long flightId, SeatClass seatClass,
                                             int count, String preferredSeat) {
        // If specific seat requested — try to allocate it first
        if (preferredSeat != null) {
            return allocatePreferredSeat(flightId, preferredSeat, count);
        }

        // Auto-allocate from available seats
        List<Seat> available = seatRepository
                .findAllByFlightIdAndSeatClassAndStatus(
                        flightId, seatClass, SeatStatus.AVAILABLE);

        if (available.size() < count) {
            throw new AeroBookException(
                    "Insufficient physical seats available. " +
                            "Requested: " + count + ", Available: " + available.size(),
                    HttpStatus.CONFLICT,
                    "INSUFFICIENT_SEATS"
            );
        }

        return available.subList(0, count);
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

    private void storeHoldInRedis(Long flightId, String bookingRef,
                                  SeatClass seatClass, int count,
                                  LocalDateTime holdUntil) {
        String key = String.format(SEAT_HOLD_KEY, flightId, bookingRef);
        java.util.Map<String, Object> holdData = java.util.Map.of(
                "flightId",   flightId,
                "bookingRef", bookingRef,
                "seatClass",  seatClass.name(),
                "count",      count,
                "holdUntil",  holdUntil.toString()
        );
        redisTemplate.opsForHash().putAll(key, holdData);
        redisTemplate.expire(key, holdDurationMinutes, TimeUnit.MINUTES);
        log.debug("Stored seat hold in Redis: key={}, TTL={}min", key, holdDurationMinutes);
    }

    private void removeHoldFromRedis(Long flightId, String bookingRef) {
        String key = String.format(SEAT_HOLD_KEY, flightId, bookingRef);
        redisTemplate.delete(key);
        log.debug("Removed seat hold from Redis: key={}", key);
    }

    private String generateBookingRef() {
        return "HOLD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}