package com.aerobook.mapper;


import com.aerobook.entity.BoardingPass;
import com.aerobook.entity.CheckIn;
import com.aerobook.domain.dto.response.BoardingPassResponse;
import com.aerobook.domain.dto.response.CheckInResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * The interface Check in mapper.
 */
@Mapper(componentModel = "spring")
public interface CheckInMapper {

    /**
     * To response check in response.
     *
     * @param checkIn the check in
     * @return the check in response
     */
    @Mapping(target = "ticketId",      source = "ticket.id")
    @Mapping(target = "ticketNumber",  source = "ticket.ticketNumber")
    @Mapping(target = "bookingId",     source = "booking.id")
    @Mapping(target = "pnr",          source = "booking.pnr")
    @Mapping(target = "passengerId",   source = "passenger.id")
    @Mapping(target = "passengerName",
            expression = "java(checkIn.getPassenger().getFirstName() + \" \" + checkIn.getPassenger().getLastName())")
    @Mapping(target = "flightId",      source = "flight.id")
    @Mapping(target = "flightNumber",  source = "flight.flightNumber")
    @Mapping(target = "originCode",    source = "flight.route.origin.iataCode")
    @Mapping(target = "destinationCode", source = "flight.route.destination.iataCode")
    @Mapping(target = "departureTime", source = "flight.departureTime")
    CheckInResponse toResponse(CheckIn checkIn);

    /**
     * To boarding pass response boarding pass response.
     *
     * @param boardingPass the boarding pass
     * @return the boarding pass response
     */
    @Mapping(target = "checkInId",     source = "checkIn.id")
    @Mapping(target = "ticketId",      source = "ticket.id")
    @Mapping(target = "passengerId",   source = "passenger.id")
    @Mapping(target = "flightId",      source = "flight.id")
    @Mapping(target = "pdfDownloadUrl",
            expression = "java(\"/boarding-passes/\" + boardingPass.getId() + \"/pdf\")")
    BoardingPassResponse toBoardingPassResponse(BoardingPass boardingPass);
}
