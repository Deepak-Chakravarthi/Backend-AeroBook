package com.aerobook.mapper;


import com.aerobook.domain.dto.request.AircraftSeatConfigRequest;
import com.aerobook.domain.dto.response.AircraftSeatConfigResponse;
import com.aerobook.enitity.AircraftSeatConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * The interface Aircraft seat config mapper.
 */
@Mapper(componentModel = "spring")
public interface AircraftSeatConfigMapper {

    /**
     * To entity aircraft seat config.
     *
     * @param request the request
     * @return the aircraft seat config
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "aircraft", ignore = true)
    AircraftSeatConfig toEntity(AircraftSeatConfigRequest request);

    /**
     * To response aircraft seat config response.
     *
     * @param config the config
     * @return the aircraft seat config response
     */
    AircraftSeatConfigResponse toResponse(AircraftSeatConfig config);
}
