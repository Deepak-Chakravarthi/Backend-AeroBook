package com.aerobook.domain.dto.request.get;


import com.aerobook.domain.enums.RouteStatus;
import com.aerobook.enitity.Route;
import com.aerobook.util.Jpa.SpecificationBuilder;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.jpa.domain.Specification;

/**
 * The type Route get request.
 */
@Getter
@Builder
public class RouteGetRequest {

    private final Long id;
    private final String originCode;
    private final String destinationCode;
    private final String status;

    /**
     * To specification specification.
     *
     * @return the specification
     */
    public Specification<Route> toSpecification() {
        return SpecificationBuilder.<Route>builder()
                .addEquals("id", id)
                .addJoinEquals("origin", "iataCode",
                        originCode != null ? originCode.toUpperCase() : null)
                .addJoinEquals("destination", "iataCode",
                        destinationCode != null ? destinationCode.toUpperCase() : null)
                .addEnumEquals("status", status, RouteStatus.class)
                .build();
    }
}