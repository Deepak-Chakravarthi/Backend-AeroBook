package com.aerobook.domain.dto.request.get;


import com.aerobook.entity.Seat;
import com.aerobook.domain.enums.SeatClass;
import com.aerobook.domain.enums.SeatStatus;
import com.aerobook.domain.enums.SeatType;
import com.aerobook.util.Jpa.SpecificationBuilder;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.jpa.domain.Specification;

@Getter
@Builder
public class SeatGetRequest {

    private final Long   flightId;
    private final String seatNumber;
    private final String seatClass;
    private final String seatType;
    private final String status;

    public Specification<Seat> toSpecification() {
        return SpecificationBuilder.<Seat>builder()
                .addJoinEquals("flight", "id", flightId)
                .addEquals("seatNumber", seatNumber)
                .addEnumEquals("seatClass", seatClass, SeatClass.class)
                .addEnumEquals("seatType", seatType, SeatType.class)
                .addEnumEquals("status", status, SeatStatus.class)
                .build();
    }
}
