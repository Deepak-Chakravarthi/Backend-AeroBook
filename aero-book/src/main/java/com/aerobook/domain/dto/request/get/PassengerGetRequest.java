package com.aerobook.domain.dto.request.get;


import com.aerobook.entity.Passenger;
import com.aerobook.domain.enums.PassengerType;
import com.aerobook.util.Jpa.SpecificationBuilder;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.jpa.domain.Specification;

@Getter
@Builder
public class PassengerGetRequest {

    private final Long   bookingId;
    private final Long   userId;
    private final String firstName;
    private final String lastName;
    private final String passengerType;
    private final String passportNumber;

    public Specification<Passenger> toSpecification() {
        return SpecificationBuilder.<Passenger>builder()
                .addJoinEquals("booking", "id", bookingId)
                .addJoinEquals("user", "id", userId)
                .addLike("firstName", firstName)
                .addLike("lastName", lastName)
                .addEquals("passportNumber", passportNumber)
                .addEnumEquals("passengerType", passengerType, PassengerType.class)
                .build();
    }
}
