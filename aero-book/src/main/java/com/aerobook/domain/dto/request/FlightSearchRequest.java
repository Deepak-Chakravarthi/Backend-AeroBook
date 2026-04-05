package com.aerobook.domain.dto.request;


import com.aerobook.domain.enums.CabinClass;
import com.aerobook.domain.enums.TripType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@Jacksonized
public class FlightSearchRequest {

    private final TripType   tripType;
    private final CabinClass cabinClass;

    @Min(value = 1, message = "At least 1 passenger required")
    private final int passengerCount;

    private final String    originCode;
    private final String    destinationCode;
    private final LocalDate departureDate;
    private final LocalDate returnDate;
    private final LocalDate departureDateFrom;
    private final LocalDate departureDateTo;

    @Valid
    private final List<FlightLegRequest> legs;

    public void validate() {
        switch (tripType) {
            case ONE_WAY    -> validateOneWay();
            case RETURN     -> validateReturn();
            case MULTI_CITY -> validateMultiCity();
        }
    }

    private void validateOneWay() {
        requireField(originCode,      "originCode is required for ONE_WAY");
        requireField(destinationCode, "destinationCode is required for ONE_WAY");
        requireDate(departureDate,    "departureDate is required for ONE_WAY");
    }

    private void validateReturn() {
        validateOneWay();
        requireDate(returnDate, "returnDate is required for RETURN trip");
        if (returnDate != null && departureDate != null
                && returnDate.isBefore(departureDate)) {
            throw new IllegalArgumentException(
                    "returnDate must be after departureDate");
        }
    }

    private void validateMultiCity() {
        if (legs == null || legs.size() < 2) {
            throw new IllegalArgumentException(
                    "MULTI_CITY requires at least 2 legs");
        }
        legs.forEach(FlightLegRequest::validate);
    }

    private void requireField(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    private void requireDate(LocalDate date, String message) {
        if (date == null) throw new IllegalArgumentException(message);
    }

    public String toCacheKey() {
        return switch (tripType) {
            case ONE_WAY -> String.format("search:oneway:%s:%s:%s:%s:%d",
                    originCode, destinationCode, departureDate,
                    cabinClass, passengerCount);
            case RETURN  -> String.format("search:return:%s:%s:%s:%s:%s:%d",
                    originCode, destinationCode, departureDate,
                    returnDate, cabinClass, passengerCount);
            case MULTI_CITY -> String.format("search:multicity:%s:%s:%d",
                    legs.stream().map(FlightLegRequest::toCacheKey)
                            .reduce("", (a, b) -> a + "-" + b),
                    cabinClass, passengerCount);
        };
    }
}