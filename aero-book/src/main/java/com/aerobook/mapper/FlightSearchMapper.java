package com.aerobook.mapper;


import com.aerobook.entity.Flight;
import com.aerobook.entity.FlightFare;
import com.aerobook.domain.dto.response.FareSearchResult;
import com.aerobook.domain.dto.response.FlightSearchResultItem;
import com.aerobook.domain.enums.CabinClass;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * The type Flight search mapper.
 */
@Component
public class FlightSearchMapper {

    /**
     * To search result flight search result item.
     *
     * @param flight         the flight
     * @param cabinClass     the cabin class
     * @param passengerCount the passenger count
     * @return the flight search result item
     */
    public FlightSearchResultItem toSearchResult(Flight flight,
                                                 CabinClass cabinClass,
                                                 int passengerCount) {
        List<FareSearchResult> fares = resolveFares(
                flight, cabinClass, passengerCount);

        int totalAvailableSeats = fares.stream()
                .mapToInt(FareSearchResult::availableSeats)
                .sum();

        return new FlightSearchResultItem(
                flight.getId(),
                flight.getFlightNumber(),
                flight.getAirline().getName(),
                flight.getAirline().getIataCode(),
                flight.getRoute().getOrigin().getIataCode(),
                flight.getRoute().getOrigin().getCity(),
                flight.getRoute().getDestination().getIataCode(),
                flight.getRoute().getDestination().getCity(),
                flight.getDepartureDate(),
                flight.getDepartureTime(),
                flight.getArrivalTime(),
                flight.getDurationMinutes(),
                flight.getDelayMinutes(),
                flight.getStatus(),
                flight.getGate(),
                flight.getTerminal(),
                fares,
                totalAvailableSeats
        );
    }

    private List<FareSearchResult> resolveFares(Flight flight,
                                                CabinClass cabinClass,
                                                int passengerCount) {
        return flight.getFares().stream()
                .filter(fare -> matchesCabinClass(fare, cabinClass))
                .map(fare -> toFareResult(fare, passengerCount))
                .toList();
    }

    private boolean matchesCabinClass(FlightFare fare, CabinClass cabinClass) {
        if (cabinClass == CabinClass.ANY) return true;
        return fare.getSeatClass().name().equals(cabinClass.name());
    }

    private FareSearchResult toFareResult(FlightFare fare, int passengerCount) {
        BigDecimal totalForAll = fare.getTotalFare()
                .multiply(BigDecimal.valueOf(passengerCount));

        return new FareSearchResult(
                fare.getSeatClass(),
                fare.getBaseFare(),
                fare.getTax(),
                fare.getTotalFare(),
                totalForAll,
                fare.getAvailableSeats(),
                fare.getAvailableSeats() >= passengerCount
        );
    }
}
