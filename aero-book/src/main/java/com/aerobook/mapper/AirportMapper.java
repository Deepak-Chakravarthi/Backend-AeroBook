package com.aerobook.mapper;


import com.aerobook.domain.dto.request.AirportRequest;
import com.aerobook.domain.dto.response.AirportResponse;
import com.aerobook.entity.Airport;
import org.mapstruct.*;

/**
 * The interface Airport mapper.
 */
@Mapper(componentModel = "spring")
public interface AirportMapper {

    /**
     * To entity airport.
     *
     * @param request the request
     * @return the airport
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Airport toEntity(AirportRequest request);

    /**
     * To response airport response.
     *
     * @param airport the airport
     * @return the airport response
     */
    AirportResponse toResponse(Airport airport);

    /**
     * Update entity.
     *
     * @param request the request
     * @param airport the airport
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(AirportRequest request, @MappingTarget Airport airport);
}
