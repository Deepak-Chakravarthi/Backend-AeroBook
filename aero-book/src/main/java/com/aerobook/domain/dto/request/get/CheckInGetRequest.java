package com.aerobook.domain.dto.request.get;


import com.aerobook.enitity.CheckIn;
import com.aerobook.domain.enums.CheckInStatus;
import com.aerobook.util.Jpa.SpecificationBuilder;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.jpa.domain.Specification;

@Getter
@Builder
public class CheckInGetRequest {

    private final Long   id;
    private final Long   ticketId;
    private final Long   bookingId;
    private final Long   passengerId;
    private final Long   flightId;
    private final String status;

    public Specification<CheckIn> toSpecification() {
        return SpecificationBuilder.<CheckIn>builder()
                .addEquals("id", id)
                .addJoinEquals("ticket", "id", ticketId)
                .addJoinEquals("booking", "id", bookingId)
                .addJoinEquals("passenger", "id", passengerId)
                .addJoinEquals("flight", "id", flightId)
                .addEnumEquals("status", status, CheckInStatus.class)
                .build();
    }
}