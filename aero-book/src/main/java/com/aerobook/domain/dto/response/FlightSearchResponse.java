package com.aerobook.domain.dto.response;


import com.aerobook.domain.enums.CabinClass;
import com.aerobook.domain.enums.TripType;

import java.time.LocalDateTime;
import java.util.List;

public record FlightSearchResponse(
        TripType tripType,
        CabinClass cabinClass,
        int passengerCount,
        List<FlightSearchResultItem> outboundFlights,
        List<FlightSearchResultItem> returnFlights,      // null for ONE_WAY
        List<List<FlightSearchResultItem>> multiCityLegs,// null for non-MULTI_CITY
        int totalResults,
        boolean fromCache,
        LocalDateTime searchedAt
) {}
