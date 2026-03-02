package com.aerobook.mapper;


import com.aerobook.domain.dto.request.AirportRequest;
import com.aerobook.domain.dto.response.AirportResponse;
import com.aerobook.enitity.Airport;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface AirportMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Airport toEntity(AirportRequest request);

    AirportResponse toResponse(Airport airport);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(AirportRequest request, @MappingTarget Airport airport);
}
