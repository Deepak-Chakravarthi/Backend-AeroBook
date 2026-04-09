package com.aerobook.mapper;


import com.aerobook.domain.dto.request.RouteRequest;
import com.aerobook.domain.dto.response.RouteResponse;
import com.aerobook.entity.Route;
import org.mapstruct.*;

/**
 * The interface Route mapper.
 */
@Mapper(componentModel = "spring", uses = {AirportMapper.class})
public interface RouteMapper {

    /**
     * To entity route.
     *
     * @param request the request
     * @return the route
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "origin", ignore = true)
    @Mapping(target = "destination", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Route toEntity(RouteRequest request);

    /**
     * To response route response.
     *
     * @param route the route
     * @return the route response
     */
    RouteResponse toResponse(Route route);

    /**
     * Update entity.
     *
     * @param request the request
     * @param route   the route
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "origin", ignore = true)
    @Mapping(target = "destination", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(RouteRequest request, @MappingTarget Route route);
}
