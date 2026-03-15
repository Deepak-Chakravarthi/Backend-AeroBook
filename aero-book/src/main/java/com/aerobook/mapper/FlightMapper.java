package com.aerobook.mapper;

import com.aerobook.enitity.Flight;
import com.aerobook.enitity.FlightFare;
import com.aerobook.enitity.FlightSchedule;
import com.aerobook.domain.dto.request.FlightFareRequest;
import com.aerobook.domain.dto.request.FlightRequest;
import com.aerobook.domain.dto.request.FlightScheduleRequest;
import com.aerobook.domain.dto.response.FlightFareResponse;
import com.aerobook.domain.dto.response.FlightResponse;
import com.aerobook.domain.dto.response.FlightScheduleResponse;
import org.mapstruct.*;

/**
 * The interface Flight mapper.
 */
@Mapper(componentModel = "spring")
public interface FlightMapper {

    /**
     * To entity flight.
     *
     * @param request the request
     * @return the flight
     */
    @Mapping(target = "id",         ignore = true)
    @Mapping(target = "airline",    ignore = true)
    @Mapping(target = "aircraft",   ignore = true)
    @Mapping(target = "route",      ignore = true)
    @Mapping(target = "schedule",   ignore = true)
    @Mapping(target = "fares",      ignore = true)
    @Mapping(target = "delayMinutes", ignore = true)
    @Mapping(target = "createdAt",  ignore = true)
    @Mapping(target = "updatedAt",  ignore = true)
    @Mapping(target = "status",
            expression = "java(request.status() != null ? request.status() : com.aerobook.domain.enums.FlightStatus.SCHEDULED)")
    Flight toEntity(FlightRequest request);

    /**
     * To response flight response.
     *
     * @param flight the flight
     * @return the flight response
     */
    @Mapping(target = "airlineId",          source = "airline.id")
    @Mapping(target = "airlineName",        source = "airline.name")
    @Mapping(target = "airlineIataCode",    source = "airline.iataCode")
    @Mapping(target = "aircraftId",         source = "aircraft.id")
    @Mapping(target = "aircraftModel",      source = "aircraft.model")
    @Mapping(target = "registrationNumber", source = "aircraft.registrationNumber")
    @Mapping(target = "routeId",            source = "route.id")
    @Mapping(target = "originCode",         source = "route.origin.iataCode")
    @Mapping(target = "originCity",         source = "route.origin.city")
    @Mapping(target = "destinationCode",    source = "route.destination.iataCode")
    @Mapping(target = "destinationCity",    source = "route.destination.city")
    @Mapping(target = "fares",              source = "fares")
    FlightResponse toResponse(Flight flight);

    /**
     * Update entity.
     *
     * @param request the request
     * @param flight  the flight
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",         ignore = true)
    @Mapping(target = "airline",    ignore = true)
    @Mapping(target = "aircraft",   ignore = true)
    @Mapping(target = "route",      ignore = true)
    @Mapping(target = "schedule",   ignore = true)
    @Mapping(target = "fares",      ignore = true)
    @Mapping(target = "createdAt",  ignore = true)
    @Mapping(target = "updatedAt",  ignore = true)
    void updateEntity(FlightRequest request, @MappingTarget Flight flight);

    /**
     * Fare to entity flight fare.
     *
     * @param request the request
     * @return the flight fare
     */
    @Mapping(target = "id",       ignore = true)
    @Mapping(target = "flight",   ignore = true)
    @Mapping(target = "totalFare",
            expression = "java(request.baseFare().add(request.tax()))")
    FlightFare fareToEntity(FlightFareRequest request);

    /**
     * Fare to response flight fare response.
     *
     * @param fare the fare
     * @return the flight fare response
     */
    FlightFareResponse fareToResponse(FlightFare fare);

    /**
     * Schedule to entity flight schedule.
     *
     * @param request the request
     * @return the flight schedule
     */
    @Mapping(target = "id",         ignore = true)
    @Mapping(target = "airline",    ignore = true)
    @Mapping(target = "aircraft",   ignore = true)
    @Mapping(target = "route",      ignore = true)
    @Mapping(target = "active",     constant = "true")
    @Mapping(target = "createdAt",  ignore = true)
    @Mapping(target = "updatedAt",  ignore = true)
    FlightSchedule scheduleToEntity(FlightScheduleRequest request);

    /**
     * Schedule to response flight schedule response.
     *
     * @param schedule the schedule
     * @return the flight schedule response
     */
    @Mapping(target = "airlineId",       source = "airline.id")
    @Mapping(target = "airlineName",     source = "airline.name")
    @Mapping(target = "routeId",         source = "route.id")
    @Mapping(target = "originCode",      source = "route.origin.iataCode")
    @Mapping(target = "destinationCode", source = "route.destination.iataCode")
    FlightScheduleResponse scheduleToResponse(FlightSchedule schedule);
}
