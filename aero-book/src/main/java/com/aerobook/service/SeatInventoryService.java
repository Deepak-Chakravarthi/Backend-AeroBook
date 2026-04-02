package com.aerobook.service;


import com.aerobook.enitity.Flight;
import com.aerobook.enitity.FlightFare;
import com.aerobook.enitity.SeatInventory;
import com.aerobook.domain.dto.response.SeatInventoryResponse;
import com.aerobook.domain.enums.SeatClass;
import com.aerobook.exception.AeroBookException;
import com.aerobook.exception.ResourceNotFoundException;
import com.aerobook.mapper.SeatMapper;
import com.aerobook.repository.SeatInventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeatInventoryService {

    private final SeatInventoryRepository inventoryRepository;
    private final SeatMapper              seatMapper;
    private final FlightService           flightService;

    public List<SeatInventoryResponse> getInventoryByFlight(Long flightId) {
        return inventoryRepository.findAllByFlightId(flightId)
                .stream()
                .map(seatMapper::toInventoryResponse)
                .toList();
    }

    public SeatInventoryResponse getInventoryByFlightAndClass(
            Long flightId, SeatClass seatClass) {
        return seatMapper.toInventoryResponse(
                findInventory(flightId, seatClass));
    }

    // ----------------------------------------------------------------
    // Initialize inventory when a flight is created
    // Called from FlightService after flight creation
    // ----------------------------------------------------------------
    @Transactional
    public void initializeInventory(Flight flight) {
        for (FlightFare fare : flight.getFares()) {
            SeatInventory inventory = SeatInventory.builder()
                    .flight(flight)
                    .seatClass(fare.getSeatClass())
                    .totalSeats(fare.getAvailableSeats())
                    .availableSeats(fare.getAvailableSeats())
                    .heldSeats(0)
                    .bookedSeats(0)
                    .blockedSeats(0)
                    .build();
            inventoryRepository.save(inventory);
            log.info("Initialized inventory for flight {} class {} — {} seats",
                    flight.getFlightNumber(), fare.getSeatClass(),
                    fare.getAvailableSeats());
        }
    }

    // ----------------------------------------------------------------
    // Hold seats — with optimistic locking retry
    // ----------------------------------------------------------------
    @Transactional
    public SeatInventory holdSeats(Long flightId, SeatClass seatClass, int count) {
        int retries = 3;
        while (retries > 0) {
            try {
                SeatInventory inventory = findInventoryWithLock(flightId, seatClass);
                inventory.holdSeats(count);
                return inventoryRepository.save(inventory);
            } catch (ObjectOptimisticLockingFailureException e) {
                retries--;
                log.warn("Optimistic lock conflict on seat inventory — retrying ({} left)",
                        retries);
                if (retries == 0) {
                    throw new AeroBookException(
                            "Seat booking conflict — please try again",
                            HttpStatus.CONFLICT,
                            "SEAT_LOCK_CONFLICT"
                    );
                }
            }
        }
        throw new AeroBookException(
                "Failed to hold seats after retries",
                HttpStatus.CONFLICT,
                "SEAT_HOLD_FAILED"
        );
    }

    // ----------------------------------------------------------------
    // Release held seats — called on booking timeout or cancellation
    // ----------------------------------------------------------------
    @Transactional
    public void releaseHeldSeats(Long flightId, SeatClass seatClass, int count) {
        SeatInventory inventory = findInventory(flightId, seatClass);
        inventory.releaseHeldSeats(count);
        inventoryRepository.save(inventory);
        log.info("Released {} held seats for flight {} class {}",
                count, flightId, seatClass);
    }

    // ----------------------------------------------------------------
    // Confirm booking — move from held to booked
    // ----------------------------------------------------------------
    @Transactional
    public void confirmBooking(Long flightId, SeatClass seatClass, int count) {
        SeatInventory inventory = findInventory(flightId, seatClass);
        inventory.confirmBooking(count);
        inventoryRepository.save(inventory);
        log.info("Confirmed {} seats for flight {} class {}",
                count, flightId, seatClass);
    }

    // ----------------------------------------------------------------
    // Cancel booking — return seats to available
    // ----------------------------------------------------------------
    @Transactional
    public void cancelBooking(Long flightId, SeatClass seatClass, int count) {
        SeatInventory inventory = findInventory(flightId, seatClass);
        inventory.cancelBooking(count);
        inventoryRepository.save(inventory);
        log.info("Cancelled {} seats for flight {} class {}",
                count, flightId, seatClass);
    }

    // ----------------------------------------------------------------
    // Internal helpers
    // ----------------------------------------------------------------
    public SeatInventory findInventory(Long flightId, SeatClass seatClass) {
        return inventoryRepository.findByFlightIdAndSeatClass(flightId, seatClass)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "SeatInventory", "flightId+seatClass",
                        flightId + "+" + seatClass));
    }

    private SeatInventory findInventoryWithLock(Long flightId, SeatClass seatClass) {
        return inventoryRepository.findByFlightIdAndSeatClassWithLock(
                        flightId, seatClass)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "SeatInventory", "flightId+seatClass",
                        flightId + "+" + seatClass));
    }
}
