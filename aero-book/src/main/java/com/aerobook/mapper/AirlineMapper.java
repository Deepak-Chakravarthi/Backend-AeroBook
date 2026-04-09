package com.aerobook.mapper;


import com.aerobook.domain.dto.request.AirlineRequest;
import com.aerobook.domain.dto.response.AirlineResponse;
import com.aerobook.entity.Airline;
import org.mapstruct.*;

/**
 * The interface Airline mapper.
 */
@Mapper(componentModel = "spring", uses = {AircraftMapper.class})
public interface AirlineMapper {

    /**
     * To entity airline.
     *
     * @param request the request
     * @return the airline
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "aircraft", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Airline toEntity(AirlineRequest request);

    /**
     * To response airline response.
     *
     * @param airline the airline
     * @return the airline response
     */
    @Mapping(target = "aircraft", source = "aircraft")
    AirlineResponse toResponse(Airline airline);

    /**
     * Update entity.
     *
     * @param request the request
     * @param airline the airline
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "aircraft", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(AirlineRequest request, @MappingTarget Airline airline);
}
