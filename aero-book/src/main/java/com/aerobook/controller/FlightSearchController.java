package com.aerobook.controller;


import com.aerobook.annotations.ExemptAuthorization;
import com.aerobook.domain.dto.request.FlightLegRequest;
import com.aerobook.domain.dto.request.FlightSearchRequest;
import com.aerobook.domain.dto.response.FlightSearchResponse;
import com.aerobook.domain.enums.CabinClass;
import com.aerobook.domain.enums.TripType;
import com.aerobook.service.FlightSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/flights/search")
@RequiredArgsConstructor
public class FlightSearchController {

    private final FlightSearchService flightSearchService;

    /**
     * Unified search endpoint — handles ONE_WAY, RETURN, MULTI_CITY via query params.
     *
     * ONE_WAY:
     *   GET /flights/search?tripType=ONE_WAY&originCode=DEL&destinationCode=BOM
     *       &departureDate=2026-03-16&cabinClass=ECONOMY&passengerCount=2
     *
     * RETURN:
     *   GET /flights/search?tripType=RETURN&originCode=DEL&destinationCode=BOM
     *       &departureDate=2026-03-16&returnDate=2026-03-20&cabinClass=ECONOMY&passengerCount=1
     *
     * DATE RANGE:
     *   GET /flights/search?tripType=ONE_WAY&originCode=DEL&destinationCode=BOM
     *       &departureDateFrom=2026-03-16&departureDateTo=2026-03-20&cabinClass=ANY&passengerCount=1
     */
    @GetMapping
    @ExemptAuthorization(reason = "Flight search is publicly accessible")
    public ResponseEntity<FlightSearchResponse> search(
            @RequestParam TripType  tripType,
            @RequestParam CabinClass cabinClass,
            @RequestParam(defaultValue = "1") int passengerCount,

            // ONE_WAY / RETURN params
            @RequestParam(required = false) String originCode,
            @RequestParam(required = false) String destinationCode,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departureDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnDate,

            // Date range params
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departureDateFrom,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departureDateTo) {

        FlightSearchRequest request = FlightSearchRequest.builder()
                .tripType(tripType)
                .cabinClass(cabinClass)
                .passengerCount(passengerCount)
                .originCode(originCode)
                .destinationCode(destinationCode)
                .departureDate(departureDate)
                .returnDate(returnDate)
                .departureDateFrom(departureDateFrom)
                .departureDateTo(departureDateTo)
                .build();

        return ResponseEntity.ok(flightSearchService.search(request));
    }

    /**
     * Multi-city search — POST because legs are a list in the body.
     *
     * POST /flights/search/multi-city
     * {
     *   "cabinClass": "ECONOMY",
     *   "passengerCount": 1,
     *   "legs": [
     *     { "originCode": "DEL", "destinationCode": "BOM", "departureDate": "2026-03-16" },
     *     { "originCode": "BOM", "destinationCode": "BLR", "departureDate": "2026-03-18" },
     *     { "originCode": "BLR", "destinationCode": "DEL", "departureDate": "2026-03-20" }
     *   ]
     * }
     */
    @PostMapping("/multi-city")
    @ExemptAuthorization(reason = "Multi-city flight search is publicly accessible")
    public ResponseEntity<FlightSearchResponse> searchMultiCity(
            @RequestBody MultiCitySearchBody body) {

        FlightSearchRequest request = FlightSearchRequest.builder()
                .tripType(TripType.MULTI_CITY)
                .cabinClass(body.cabinClass())
                .passengerCount(body.passengerCount())
                .legs(body.legs())
                .build();

        return ResponseEntity.ok(flightSearchService.search(request));
    }

    // Inner record for multi-city POST body
    public record MultiCitySearchBody(
            CabinClass cabinClass,
            int passengerCount,
            List<FlightLegRequest> legs
    ) {}
}
