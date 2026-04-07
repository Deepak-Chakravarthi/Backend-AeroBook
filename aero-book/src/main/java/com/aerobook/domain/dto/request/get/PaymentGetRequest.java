package com.aerobook.domain.dto.request.get;


import com.aerobook.enitity.Payment;
import com.aerobook.domain.enums.PaymentMethod;
import com.aerobook.domain.enums.PaymentStatus;
import com.aerobook.util.Jpa.SpecificationBuilder;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.jpa.domain.Specification;

@Getter
@Builder
public class PaymentGetRequest {

    private final Long   id;
    private final String paymentReference;
    private final Long   bookingId;
    private final Long   userId;
    private final String status;
    private final String paymentMethod;

    public Specification<Payment> toSpecification() {
        return SpecificationBuilder.<Payment>builder()
                .addEquals("id", id)
                .addEquals("paymentReference", paymentReference)
                .addJoinEquals("booking", "id", bookingId)
                .addJoinEquals("user", "id", userId)
                .addEnumEquals("status", status, PaymentStatus.class)
                .addEnumEquals("paymentMethod", paymentMethod, PaymentMethod.class)
                .build();
    }
}
