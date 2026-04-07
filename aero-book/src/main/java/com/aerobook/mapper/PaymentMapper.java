package com.aerobook.mapper;


import com.aerobook.enitity.Payment;
import com.aerobook.enitity.Refund;
import com.aerobook.domain.dto.response.PaymentResponse;
import com.aerobook.domain.dto.response.RefundResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "bookingId",  source = "booking.id")
    @Mapping(target = "pnr",        source = "booking.pnr")
    @Mapping(target = "userId",     source = "user.id")
    @Mapping(target = "username",   source = "user.username")
    @Mapping(target = "refunds",    source = "refunds")
    PaymentResponse toResponse(Payment payment);

    @Mapping(target = "paymentId",        source = "payment.id")
    @Mapping(target = "paymentReference", source = "payment.paymentReference")
    @Mapping(target = "bookingId",        source = "booking.id")
    @Mapping(target = "pnr",             source = "booking.pnr")
    RefundResponse toRefundResponse(Refund refund);
}
