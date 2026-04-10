package com.aerobook.event;


import com.aerobook.entity.Flight;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class FlightCompletedEvent extends ApplicationEvent {

    private final Flight flight;

    public FlightCompletedEvent(Object source, Flight flight) {
        super(source);
        this.flight = flight;
    }

}