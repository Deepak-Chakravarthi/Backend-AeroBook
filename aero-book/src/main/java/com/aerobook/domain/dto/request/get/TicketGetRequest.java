package com.aerobook.domain.dto.request.get;


import com.aerobook.entity.Ticket;
import com.aerobook.domain.enums.SeatClass;
import com.aerobook.domain.enums.TicketStatus;
import com.aerobook.util.Jpa.SpecificationBuilder;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.jpa.domain.Specification;

@Getter
@Builder
public class TicketGetRequest {

    private final Long    id;
    private final String  ticketNumber;
    private final Long    bookingId;
    private final Long    passengerId;
    private final Long    flightId;
    private final String  seatClass;
    private final String  status;
    private final Boolean isReturnLeg;

    public Specification<Ticket> toSpecification() {
        return SpecificationBuilder.<Ticket>builder()
                .addEquals("id", id)
                .addEquals("ticketNumber", ticketNumber)
                .addJoinEquals("booking", "id", bookingId)
                .addJoinEquals("passenger", "id", passengerId)
                .addJoinEquals("flight", "id", flightId)
                .addEnumEquals("seatClass", seatClass, SeatClass.class)
                .addEnumEquals("status", status, TicketStatus.class)
                .addEquals("isReturnLeg", isReturnLeg)
                .build();
    }
}
