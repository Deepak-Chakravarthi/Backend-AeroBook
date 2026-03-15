package com.aerobook.service;

import com.aerobook.enitity.Flight;
import com.aerobook.enitity.FlightFare;
import com.aerobook.domain.dto.request.FlightFareRequest;
import com.aerobook.domain.dto.response.FlightFareResponse;
import com.aerobook.exception.AeroBookException;
import com.aerobook.exception.DuplicateResourceException;
import com.aerobook.exception.ResourceNotFoundException;
import com.aerobook.mapper.FlightMapper;
import com.aerobook.repository.FlightFareRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FlightFareService {

    private final FlightFareRepository flightFareRepository;
    private final FlightMapper         flightMapper;
    private final FlightService        flightService;

    public List<FlightFareResponse> getFaresByFlight(Long flightId) {
        return flightFareRepository.findAllByFlightId(flightId)
                .stream()
                .map(flightMapper::fareToResponse)
                .toList();
    }

    @Transactional
    public FlightFareResponse addFare(Long flightId, FlightFareRequest request) {
        Flight flight = flightService.findFlightById(flightId);

        if (flightFareRepository.existsByFlightIdAndSeatClass(
                flightId, request.seatClass())) {
            throw new DuplicateResourceException(
                    "FlightFare", "seatClass", request.seatClass().name());
        }

        validateSeatCountAgainstAircraft(flight, request);

        FlightFare fare = flightMapper.fareToEntity(request);
        fare.setFlight(flight);

        return flightMapper.fareToResponse(flightFareRepository.save(fare));
    }

    @Transactional
    public FlightFareResponse updateFare(Long fareId, FlightFareRequest request) {
        FlightFare fare = flightFareRepository.findById(fareId)
                .orElseThrow(() -> new ResourceNotFoundException("FlightFare", fareId));

        fare.setBaseFare(request.baseFare());
        fare.setTax(request.tax());
        fare.setTotalFare(request.baseFare().add(request.tax()));
        fare.setAvailableSeats(request.availableSeats());

        return flightMapper.fareToResponse(flightFareRepository.save(fare));
    }

    @Transactional
    public void deleteFare(Long fareId) {
        if (!flightFareRepository.existsById(fareId)) {
            throw new ResourceNotFoundException("FlightFare", fareId);
        }
        flightFareRepository.deleteById(fareId);
    }

    // ----------------------------------------------------------------
    // Validate available seats don't exceed aircraft seat config
    // ----------------------------------------------------------------
    private void validateSeatCountAgainstAircraft(Flight flight,
                                                  FlightFareRequest request) {
        flight.getAircraft().getSeatConfigs().stream()
                .filter(sc -> sc.getSeatClass().equals(request.seatClass()))
                .findFirst()
                .ifPresentOrElse(config -> {
                    if (request.availableSeats() > config.getSeatCount()) {
                        throw new AeroBookException(
                                "Available seats " + request.availableSeats()
                                        + " exceeds aircraft capacity "
                                        + config.getSeatCount()
                                        + " for class " + request.seatClass(),
                                HttpStatus.BAD_REQUEST,
                                "SEATS_EXCEED_CAPACITY"
                        );
                    }
                }, () -> {
                    throw new AeroBookException(
                            "Aircraft has no " + request.seatClass()
                                    + " class configuration",
                            HttpStatus.BAD_REQUEST,
                            "SEAT_CLASS_NOT_CONFIGURED"
                    );
                });
    }
}