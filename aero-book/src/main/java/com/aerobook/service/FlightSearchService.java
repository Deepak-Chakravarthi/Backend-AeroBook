package com.aerobook.service;


import com.aerobook.enitity.Flight;
import com.aerobook.domain.dto.request.FlightLegRequest;
import com.aerobook.domain.dto.request.FlightSearchRequest;
import com.aerobook.domain.dto.response.FlightSearchResponse;
import com.aerobook.domain.dto.response.FlightSearchResultItem;
import com.aerobook.domain.enums.TripType;
import com.aerobook.enitity.Flight;
import com.aerobook.mapper.FlightSearchMapper;
import com.aerobook.repository.FlightSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FlightSearchService {

    private final FlightSearchRepository flightSearchRepository;
    private final FlightSearchMapper     flightSearchMapper;
    private final FlightSearchCacheService cacheService;

    // ----------------------------------------------------------------
    // Main search entry point
    // ----------------------------------------------------------------
    public FlightSearchResponse search(FlightSearchRequest request) {
        request.validate();

        String cacheKey = request.toCacheKey();

        // Step 1 — check cache first
        Optional<FlightSearchResponse> cached = cacheService.get(cacheKey);
        if (cached.isPresent()) {
            return withCacheFlag(cached.get(), true);
        }

        // Step 2 — search DB
        FlightSearchResponse response = switch (request.getTripType()) {
            case ONE_WAY    -> searchOneWay(request);
            case RETURN     -> searchReturn(request);
            case MULTI_CITY -> searchMultiCity(request);
        };

        // Step 3 — store in cache
        cacheService.put(cacheKey, response);

        return response;
    }

    // ----------------------------------------------------------------
    // One-way search
    // ----------------------------------------------------------------
    private FlightSearchResponse searchOneWay(FlightSearchRequest request) {
        List<Flight> flights = resolveOneWayFlights(request);

        List<FlightSearchResultItem> results = flights.stream()
                .filter(f -> hasSufficientSeats(
                        f, request.getCabinClass(), request.getPassengerCount()))
                .map(f -> flightSearchMapper.toSearchResult(
                        f, request.getCabinClass(), request.getPassengerCount()))
                .toList();

        log.info("ONE_WAY search {}→{} on {} — {} results found",
                request.getOriginCode(), request.getDestinationCode(),
                request.getDepartureDate(), results.size());

        return new FlightSearchResponse(
                TripType.ONE_WAY,
                request.getCabinClass(),
                request.getPassengerCount(),
                results,
                null,
                null,
                results.size(),
                false,
                LocalDateTime.now()
        );
    }

    // ----------------------------------------------------------------
    // Return search
    // ----------------------------------------------------------------
    private FlightSearchResponse searchReturn(FlightSearchRequest request) {
        // Outbound — origin → destination
        List<com.aerobook.enitity.Flight> outboundFlights = resolveOneWayFlights(request);

        // Inbound — destination → origin on return date
        List<com.aerobook.enitity.Flight> inboundFlights = flightSearchRepository.searchReturn(
                request.getDestinationCode(),
                request.getOriginCode(),
                request.getReturnDate()
        );

        List<FlightSearchResultItem> outbound = outboundFlights.stream()
                .filter(f -> hasSufficientSeats(
                        f, request.getCabinClass(), request.getPassengerCount()))
                .map(f -> flightSearchMapper.toSearchResult(
                        f, request.getCabinClass(), request.getPassengerCount()))
                .toList();

        List<FlightSearchResultItem> returnFlights = inboundFlights.stream()
                .filter(f -> hasSufficientSeats(
                        f, request.getCabinClass(), request.getPassengerCount()))
                .map(f -> flightSearchMapper.toSearchResult(
                        f, request.getCabinClass(), request.getPassengerCount()))
                .toList();

        int totalResults = outbound.size() + returnFlights.size();

        log.info("RETURN search {}↔{} — outbound: {}, return: {}",
                request.getOriginCode(), request.getDestinationCode(),
                outbound.size(), returnFlights.size());

        return new FlightSearchResponse(
                TripType.RETURN,
                request.getCabinClass(),
                request.getPassengerCount(),
                outbound,
                returnFlights,
                null,
                totalResults,
                false,
                LocalDateTime.now()
        );
    }

    // ----------------------------------------------------------------
    // Multi-city search
    // ----------------------------------------------------------------
    private FlightSearchResponse searchMultiCity(FlightSearchRequest request) {
        List<List<FlightSearchResultItem>> multiCityLegs = new ArrayList<>();

        for (FlightLegRequest leg : request.getLegs()) {
            List<com.aerobook.enitity.Flight> legFlights = flightSearchRepository.searchLeg(
                    leg.getOriginCode().toUpperCase(),
                    leg.getDestinationCode().toUpperCase(),
                    leg.getDepartureDate()
            );

            List<FlightSearchResultItem> legResults = legFlights.stream()
                    .filter(f -> hasSufficientSeats(
                            f, request.getCabinClass(), request.getPassengerCount()))
                    .map(f -> flightSearchMapper.toSearchResult(
                            f, request.getCabinClass(), request.getPassengerCount()))
                    .toList();

            multiCityLegs.add(legResults);

            log.info("MULTI_CITY leg {}→{} on {} — {} results",
                    leg.getOriginCode(), leg.getDestinationCode(),
                    leg.getDepartureDate(), legResults.size());
        }

        int totalResults = multiCityLegs.stream()
                .mapToInt(List::size)
                .sum();

        return new FlightSearchResponse(
                TripType.MULTI_CITY,
                request.getCabinClass(),
                request.getPassengerCount(),
                null,
                null,
                multiCityLegs,
                totalResults,
                false,
                LocalDateTime.now()
        );
    }

    // ----------------------------------------------------------------
    // Private helpers
    // ----------------------------------------------------------------

    private List<com.aerobook.enitity.Flight> resolveOneWayFlights(FlightSearchRequest request) {
        String origin      = request.getOriginCode().toUpperCase();
        String destination = request.getDestinationCode().toUpperCase();

        // Date range search if range provided
        if (request.getDepartureDateFrom() != null
                && request.getDepartureDateTo() != null) {
            return flightSearchRepository.searchOneWayDateRange(
                    origin, destination,
                    request.getDepartureDateFrom(),
                    request.getDepartureDateTo()
            );
        }

        // Exact date search
        return flightSearchRepository.searchOneWay(
                origin, destination, request.getDepartureDate());
    }

    private boolean hasSufficientSeats(com.aerobook.enitity.Flight flight,
                                       com.aerobook.domain.enums.CabinClass cabinClass,
                                       int passengerCount) {
        if (flight.getFares() == null || flight.getFares().isEmpty()) return false;

        return flight.getFares().stream()
                .filter(fare -> matchesCabin(fare.getSeatClass(), cabinClass))
                .anyMatch(fare -> fare.getAvailableSeats() >= passengerCount);
    }

    private boolean matchesCabin(com.aerobook.domain.enums.SeatClass seatClass,
                                 com.aerobook.domain.enums.CabinClass cabinClass) {
        if (cabinClass == com.aerobook.domain.enums.CabinClass.ANY) return true;
        return seatClass.name().equals(cabinClass.name());
    }

    private FlightSearchResponse withCacheFlag(FlightSearchResponse response,
                                               boolean fromCache) {
        return new FlightSearchResponse(
                response.tripType(),
                response.cabinClass(),
                response.passengerCount(),
                response.outboundFlights(),
                response.returnFlights(),
                response.multiCityLegs(),
                response.totalResults(),
                fromCache,
                response.searchedAt()
        );
    }
}