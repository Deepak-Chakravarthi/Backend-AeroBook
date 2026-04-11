package com.aerobook.event;


import com.aerobook.entity.Payment;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PaymentSuccessEvent extends ApplicationEvent {

    private final Payment payment;

    public PaymentSuccessEvent(Object source, Payment payment) {
        super(source);
        this.payment = payment;
    }
}