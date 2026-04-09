package com.aerobook.mapper;

import com.aerobook.entity.Passenger;
import com.aerobook.entity.Ticket;
import com.aerobook.domain.dto.request.PassengerRequest;
import com.aerobook.domain.dto.response.PassengerResponse;
import com.aerobook.domain.dto.response.TicketResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PassengerMapper {

    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "booking",   ignore = true)
    @Mapping(target = "user",      ignore = true)
    @Mapping(target = "tickets",   ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Passenger toEntity(PassengerRequest request);

    @Mapping(target = "bookingId", source = "booking.id")
    @Mapping(target = "pnr",       source = "booking.pnr")
    @Mapping(target = "userId",    source = "user.id")
    PassengerResponse toResponse(Passenger passenger);

    @Mapping(target = "bookingId",     source = "booking.id")
    @Mapping(target = "pnr",           source = "booking.pnr")
    @Mapping(target = "passengerId",   source = "passenger.id")
    @Mapping(target = "passengerName",
            expression = "java(ticket.getPassenger().getFirstName() + \" \" + ticket.getPassenger().getLastName())")
    @Mapping(target = "flightId",      source = "flight.id")
    @Mapping(target = "flightNumber",  source = "flight.flightNumber")
    @Mapping(target = "originCode",    source = "flight.route.origin.iataCode")
    @Mapping(target = "destinationCode", source = "flight.route.destination.iataCode")
    @Mapping(target = "departureTime", source = "flight.departureTime")
    @Mapping(target = "arrivalTime",   source = "flight.arrivalTime")
    TicketResponse toTicketResponse(Ticket ticket);
}
