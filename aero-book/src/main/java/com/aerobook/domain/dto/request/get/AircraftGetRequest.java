package com.aerobook.domain.dto.request.get;

import com.aerobook.domain.enums.AircraftStatus;
import com.aerobook.enitity.Aircraft;
import com.aerobook.util.Jpa.SpecificationBuilder;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.jpa.domain.Specification;


/**
 * The type Aircraft get request.
 */
@Getter
@Builder
public class AircraftGetRequest {

    private final Long id;
    private final String registrationNumber;
    private final String model;
    private final String manufacturer;
    private final Long airlineId;
    private final String status;


    /**
     * To specification specification.
     *
     * @return the specification
     */
    public Specification<Aircraft> toSpecification() {
        return SpecificationBuilder.<Aircraft>builder()
                .addEquals("id", id)
                .addEquals("registrationNumber", registrationNumber)
                .addLike("model", model)
                .addLike("manufacturer", manufacturer)
                .addJoinEquals("airline", "id", airlineId)
                .addEnumEquals("status", status, AircraftStatus.class)
                .build();
    }

}
