package com.aerobook.mapper;


import com.aerobook.domain.dto.request.AircraftRequest;
import com.aerobook.domain.dto.response.AircraftResponse;
import com.aerobook.domain.dto.response.AircraftSummaryResponse;
import com.aerobook.enitity.Aircraft;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {AircraftSeatConfigMapper.class})
public interface AircraftMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "airline", ignore = true)
    @Mapping(target = "seatConfigs", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Aircraft toEntity(AircraftRequest request);

    @Mapping(target = "airlineId", source = "airline.id")
    @Mapping(target = "airlineName", source = "airline.name")
    @Mapping(target = "seatConfigs", source = "seatConfigs")
    AircraftResponse toResponse(Aircraft aircraft);

    AircraftSummaryResponse toSummaryResponse(Aircraft aircraft);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "airline", ignore = true)
    @Mapping(target = "seatConfigs", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(AircraftRequest request, @MappingTarget Aircraft aircraft);
}