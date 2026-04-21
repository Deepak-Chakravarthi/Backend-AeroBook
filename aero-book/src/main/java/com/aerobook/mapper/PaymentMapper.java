package com.aerobook.mapper;


import com.aerobook.entity.Payment;
import com.aerobook.entity.Refund;
import com.aerobook.domain.dto.response.PaymentResponse;
import com.aerobook.domain.dto.response.RefundResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * The interface Payment mapper.
 */
@Mapper(componentModel = "spring")
public interface PaymentMapper {

    /**
     * To response payment response.
     *
     * @param payment the payment
     * @return the payment response
     */
    @Mapping(target = "bookingId",  source = "booking.id")
    @Mapping(target = "pnr",        source = "booking.pnr")
    @Mapping(target = "userId",     source = "user.id")
    @Mapping(target = "username",   source = "user.username")
    @Mapping(target = "refunds",    source = "refunds")
    PaymentResponse toResponse(Payment payment);

    /**
     * To refund response refund response.
     *
     * @param refund the refund
     * @return the refund response
     */
    @Mapping(target = "paymentId",        source = "payment.id")
    @Mapping(target = "paymentReference", source = "payment.paymentReference")
    @Mapping(target = "bookingId",        source = "booking.id")
    @Mapping(target = "pnr",             source = "booking.pnr")
    RefundResponse toRefundResponse(Refund refund);
}
