package com.aerobook.mapper;


import com.aerobook.entity.Seat;
import com.aerobook.entity.SeatInventory;
import com.aerobook.domain.dto.response.SeatInventoryResponse;
import com.aerobook.domain.dto.response.SeatResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * The interface Seat mapper.
 */
@Mapper(componentModel = "spring")
public interface SeatMapper {

    /**
     * To inventory response seat inventory response.
     *
     * @param inventory the inventory
     * @return the seat inventory response
     */
    @Mapping(target = "flightId",     source = "flight.id")
    @Mapping(target = "flightNumber", source = "flight.flightNumber")
    SeatInventoryResponse toInventoryResponse(SeatInventory inventory);

    /**
     * To seat response seat response.
     *
     * @param seat the seat
     * @return the seat response
     */
    @Mapping(target = "flightId",     source = "flight.id")
    @Mapping(target = "flightNumber", source = "flight.flightNumber")
    SeatResponse toSeatResponse(Seat seat);
}
