package com.aerobook.mapper;


import com.aerobook.enitity.AircraftSeatConfig;
import com.aerobook.domain.dto.request.AircraftSeatConfigRequest;
import com.aerobook.domain.dto.response.AircraftSeatConfigResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface AircraftSeatConfigMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "aircraft", ignore = true)
    AircraftSeatConfig toEntity(AircraftSeatConfigRequest request);

    AircraftSeatConfigResponse toResponse(AircraftSeatConfig config);
}
