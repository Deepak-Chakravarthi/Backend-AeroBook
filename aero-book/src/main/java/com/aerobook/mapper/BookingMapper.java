package com.aerobook.mapper;


import com.aerobook.entity.Booking;
import com.aerobook.domain.dto.response.BookingResponse;
import com.aerobook.domain.dto.response.BookingSummaryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * The interface Booking mapper.
 */
@Mapper(componentModel = "spring")
public interface BookingMapper {

    /**
     * To response booking response.
     *
     * @param booking the booking
     * @return the booking response
     */
    @Mapping(target = "userId",   source = "user.id")
    @Mapping(target = "username", source = "user.username")

    @Mapping(target = "outboundFlightId",        source = "outboundFlight.id")
    @Mapping(target = "outboundFlightNumber",     source = "outboundFlight.flightNumber")
    @Mapping(target = "outboundOriginCode",       source = "outboundFlight.route.origin.iataCode")
    @Mapping(target = "outboundDestinationCode",  source = "outboundFlight.route.destination.iataCode")
    @Mapping(target = "outboundDepartureTime",    source = "outboundFlight.departureTime")
    @Mapping(target = "outboundArrivalTime",      source = "outboundFlight.arrivalTime")

    @Mapping(target = "returnFlightId",           source = "returnFlight.id")
    @Mapping(target = "returnFlightNumber",       source = "returnFlight.flightNumber")
    @Mapping(target = "returnOriginCode",         source = "returnFlight.route.origin.iataCode")
    @Mapping(target = "returnDestinationCode",    source = "returnFlight.route.destination.iataCode")
    @Mapping(target = "returnDepartureTime",      source = "returnFlight.departureTime")
    @Mapping(target = "returnArrivalTime",        source = "returnFlight.arrivalTime")

    BookingResponse toResponse(Booking booking);

    /**
     * To summary response booking summary response.
     *
     * @param booking the booking
     * @return the booking summary response
     */
    @Mapping(target = "outboundFlightNumber",    source = "outboundFlight.flightNumber")
    @Mapping(target = "outboundOriginCode",      source = "outboundFlight.route.origin.iataCode")
    @Mapping(target = "outboundDestinationCode", source = "outboundFlight.route.destination.iataCode")
    @Mapping(target = "outboundDepartureTime",   source = "outboundFlight.departureTime")
    @Mapping(target = "passengerFirstName",      source = "passengerFirstName")
    @Mapping(target = "passengerLastName",       source = "passengerLastName")
    BookingSummaryResponse toSummaryResponse(Booking booking);
}