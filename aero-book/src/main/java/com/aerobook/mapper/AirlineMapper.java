package com.aerobook.mapper;


import com.aerobook.enitity.Airline;
import com.aerobook.domain.dto.request.AirlineRequest;
import com.aerobook.domain.dto.response.AirlineResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {AircraftMapper.class})
public interface AirlineMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "aircraft", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Airline toEntity(AirlineRequest request);

    @Mapping(target = "aircraft", source = "aircraft")
    AirlineResponse toResponse(Airline airline);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "aircraft", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(AirlineRequest request, @MappingTarget Airline airline);
}
