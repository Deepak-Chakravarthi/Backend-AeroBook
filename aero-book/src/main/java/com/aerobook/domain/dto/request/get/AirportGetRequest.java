package com.aerobook.domain.dto.request.get;

import com.aerobook.entity.Airport;
import com.aerobook.util.Jpa.SpecificationBuilder;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.jpa.domain.Specification;

/**
 * The type Airport get request.
 */
@Getter
@Builder
public class AirportGetRequest {

    private final Long id;
    private final String iataCode;
    private final String name;
    private final String city;
    private final String country;
    private final String timezone;


    /**
     * To specification specification.
     *
     * @return the specification
     */
    public Specification<Airport> toSpecification() {
        return SpecificationBuilder.<Airport>builder()
                .addEquals("id", id)
                .addEquals("iataCode", iataCode != null ? iataCode.toUpperCase() : null)
                .addLike("name", name)
                .addLike("city", city)
                .addLike("country", country)
                .addEquals("timezone", timezone)
                .build();
    }
}