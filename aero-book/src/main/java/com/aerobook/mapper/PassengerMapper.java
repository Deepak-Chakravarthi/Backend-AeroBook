package com.aerobook.mapper;

import com.aerobook.entity.Passenger;
import com.aerobook.entity.Ticket;
import com.aerobook.domain.dto.request.PassengerRequest;
import com.aerobook.domain.dto.response.PassengerResponse;
import com.aerobook.domain.dto.response.TicketResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * The interface Passenger mapper.
 */
@Mapper(componentModel = "spring")
public interface PassengerMapper {

    /**
     * To entity passenger.
     *
     * @param request the request
     * @return the passenger
     */
    @Mapping(target = "id",        ignore = true)
    @Mapping(target = "booking",   ignore = true)
    @Mapping(target = "user",      ignore = true)
    @Mapping(target = "tickets",   ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Passenger toEntity(PassengerRequest request);

    /**
     * To response passenger response.
     *
     * @param passenger the passenger
     * @return the passenger response
     */
    @Mapping(target = "bookingId", source = "booking.id")
    @Mapping(target = "pnr",       source = "booking.pnr")
    @Mapping(target = "userId",    source = "user.id")
    PassengerResponse toResponse(Passenger passenger);

    /**
     * To ticket response ticket response.
     *
     * @param ticket the ticket
     * @return the ticket response
     */
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
