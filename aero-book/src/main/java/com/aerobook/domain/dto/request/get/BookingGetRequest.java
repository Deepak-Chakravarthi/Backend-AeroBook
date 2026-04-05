package com.aerobook.domain.dto.request.get;


import com.aerobook.enitity.Booking;
import com.aerobook.domain.enums.BookingStatus;
import com.aerobook.domain.enums.BookingType;
import com.aerobook.util.Jpa.SpecificationBuilder;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.jpa.domain.Specification;

@Getter
@Builder
public class BookingGetRequest {

    private final Long   id;
    private final String pnr;
    private final Long   userId;
    private final String status;
    private final String bookingType;

    public Specification<Booking> toSpecification() {
        return SpecificationBuilder.<Booking>builder()
                .addEquals("id", id)
                .addEquals("pnr", pnr)
                .addJoinEquals("user", "id", userId)
                .addEnumEquals("status", status, BookingStatus.class)
                .addEnumEquals("bookingType", bookingType, BookingType.class)
                .build();
    }
}