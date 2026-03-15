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

@Mapper(componentModel = "spring")
public interface FlightMapper {

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
            expression = "java(request.status() != null ? request.status() : com.aerobook.enums.FlightStatus.SCHEDULED)")
    Flight toEntity(FlightRequest request);

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

    @Mapping(target = "id",       ignore = true)
    @Mapping(target = "flight",   ignore = true)
    @Mapping(target = "totalFare",
            expression = "java(request.baseFare().add(request.tax()))")
    FlightFare fareToEntity(FlightFareRequest request);

    FlightFareResponse fareToResponse(FlightFare fare);

    @Mapping(target = "id",         ignore = true)
    @Mapping(target = "airline",    ignore = true)
    @Mapping(target = "aircraft",   ignore = true)
    @Mapping(target = "route",      ignore = true)
    @Mapping(target = "active",     constant = "true")
    @Mapping(target = "createdAt",  ignore = true)
    @Mapping(target = "updatedAt",  ignore = true)
    FlightSchedule scheduleToEntity(FlightScheduleRequest request);

    @Mapping(target = "airlineId",       source = "airline.id")
    @Mapping(target = "airlineName",     source = "airline.name")
    @Mapping(target = "routeId",         source = "route.id")
    @Mapping(target = "originCode",      source = "route.origin.iataCode")
    @Mapping(target = "destinationCode", source = "route.destination.iataCode")
    FlightScheduleResponse scheduleToResponse(FlightSchedule schedule);
}
