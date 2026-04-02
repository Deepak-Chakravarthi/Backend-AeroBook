package com.aerobook.mapper;


import com.aerobook.enitity.Seat;
import com.aerobook.enitity.SeatInventory;
import com.aerobook.domain.dto.response.SeatInventoryResponse;
import com.aerobook.domain.dto.response.SeatResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SeatMapper {

    @Mapping(target = "flightId",     source = "flight.id")
    @Mapping(target = "flightNumber", source = "flight.flightNumber")
    SeatInventoryResponse toInventoryResponse(SeatInventory inventory);

    @Mapping(target = "flightId",     source = "flight.id")
    @Mapping(target = "flightNumber", source = "flight.flightNumber")
    SeatResponse toSeatResponse(Seat seat);
}
