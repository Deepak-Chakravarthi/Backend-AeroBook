package com.aerobook.domain.dto.request.get;


import com.aerobook.enitity.Flight;
import com.aerobook.domain.enums.FlightStatus;
import com.aerobook.util.Jpa.SpecificationBuilder;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

/**
 * The type Flight get request.
 */
@Getter
@Builder
public class FlightGetRequest {

    private final Long       id;
    private final String     flightNumber;
    private final Long       airlineId;
    private final Long       aircraftId;
    private final Long       routeId;
    private final LocalDate  departureDate;
    private final String     status;
    private final String     originCode;
    private final String     destinationCode;

    /**
     * To specification specification.
     *
     * @return the specification
     */
    public Specification<Flight> toSpecification() {
        return SpecificationBuilder.<Flight>builder()
                .addEquals("id", id)
                .addEquals("flightNumber", flightNumber)
                .addJoinEquals("airline", "id", airlineId)
                .addJoinEquals("aircraft", "id", aircraftId)
                .addJoinEquals("route", "id", routeId)
                .addEquals("departureDate", departureDate)
                .addEnumEquals("status", status, FlightStatus.class)
                .addJoinEquals("route.origin", "iataCode",
                        originCode != null ? originCode.toUpperCase() : null)
                .addJoinEquals("route.destination", "iataCode",
                        destinationCode != null ? destinationCode.toUpperCase() : null)
                .build();
    }
}
