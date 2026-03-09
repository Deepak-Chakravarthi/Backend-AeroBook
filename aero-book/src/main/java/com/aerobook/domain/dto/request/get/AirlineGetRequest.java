package com.aerobook.domain.dto.request.get;

import com.aerobook.domain.enums.AirlineStatus;
import com.aerobook.enitity.Airline;
import com.aerobook.util.Jpa.SpecificationBuilder;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.jpa.domain.Specification;

/**
 * The type Airline get request.
 */
@Getter
@Builder
public class AirlineGetRequest {

    private final Long id;
    private final String iataCode;
    private final String icaoCode;
    private final String name;
    private final String country;
    private final String status;

    /**
     * To specification specification.
     *
     * @return the specification
     */
    public Specification<Airline> toSpecification() {
        return SpecificationBuilder.<Airline>builder()
                .addEquals("id", id)
                .addEquals("iataCode", iataCode != null ? iataCode.toUpperCase() : null)
                .addEquals("icaoCode", icaoCode != null ? icaoCode.toUpperCase() : null)
                .addLike("name", name)
                .addLike("country", country)
                .addEnumEquals("status", status, AirlineStatus.class)
                .build();
    }

}
