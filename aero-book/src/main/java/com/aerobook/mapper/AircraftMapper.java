package com.aerobook.mapper;


import com.aerobook.domain.dto.request.AircraftRequest;
import com.aerobook.domain.dto.response.AircraftResponse;
import com.aerobook.domain.dto.response.AircraftSummaryResponse;
import com.aerobook.enitity.Aircraft;
import org.mapstruct.*;

/**
 * The interface Aircraft mapper.
 */
@Mapper(componentModel = "spring", uses = {AircraftSeatConfigMapper.class})
public interface AircraftMapper {

    /**
     * To entity aircraft.
     *
     * @param request the request
     * @return the aircraft
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "airline", ignore = true)
    @Mapping(target = "seatConfigs", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Aircraft toEntity(AircraftRequest request);

    /**
     * To response aircraft response.
     *
     * @param aircraft the aircraft
     * @return the aircraft response
     */
    @Mapping(target = "airlineId", source = "airline.id")
    @Mapping(target = "airlineName", source = "airline.name")
    @Mapping(target = "seatConfigs", source = "seatConfigs")
    AircraftResponse toResponse(Aircraft aircraft);

    /**
     * To summary response aircraft summary response.
     *
     * @param aircraft the aircraft
     * @return the aircraft summary response
     */
    AircraftSummaryResponse toSummaryResponse(Aircraft aircraft);

    /**
     * Update entity.
     *
     * @param request  the request
     * @param aircraft the aircraft
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "airline", ignore = true)
    @Mapping(target = "seatConfigs", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(AircraftRequest request, @MappingTarget Aircraft aircraft);
}