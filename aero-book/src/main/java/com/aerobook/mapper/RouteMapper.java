package com.aerobook.mapper;


import com.aerobook.domain.dto.request.RouteRequest;
import com.aerobook.domain.dto.response.RouteResponse;
import com.aerobook.enitity.Route;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {AirportMapper.class})
public interface RouteMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "origin", ignore = true)
    @Mapping(target = "destination", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Route toEntity(RouteRequest request);

    RouteResponse toResponse(Route route);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "origin", ignore = true)
    @Mapping(target = "destination", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(RouteRequest request, @MappingTarget Route route);
}
