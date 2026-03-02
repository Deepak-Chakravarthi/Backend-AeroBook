package com.aerobook.mapper;


import com.aerobook.domain.dto.request.AircraftSeatConfigRequest;
import com.aerobook.domain.dto.response.AircraftSeatConfigResponse;
import com.aerobook.enitity.AircraftSeatConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AircraftSeatConfigMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "aircraft", ignore = true)
    AircraftSeatConfig toEntity(AircraftSeatConfigRequest request);

    AircraftSeatConfigResponse toResponse(AircraftSeatConfig config);
}
