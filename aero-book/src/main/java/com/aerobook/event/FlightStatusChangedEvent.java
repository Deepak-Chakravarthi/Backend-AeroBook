package com.aerobook.event;


import com.aerobook.entity.Booking;
import com.aerobook.entity.Flight;
import com.aerobook.domain.enums.FlightStatus;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
public class FlightStatusChangedEvent extends ApplicationEvent {

    private final Flight           flight;
    private final FlightStatus     previousStatus;
    private final FlightStatus     newStatus;
    private final String           reason;
    private final List<Booking>    affectedBookings;

    public FlightStatusChangedEvent(Object source,
                                    Flight flight,
                                    FlightStatus previousStatus,
                                    FlightStatus newStatus,
                                    String reason,
                                    List<Booking> affectedBookings) {
        super(source);
        this.flight           = flight;
        this.previousStatus   = previousStatus;
        this.newStatus        = newStatus;
        this.reason           = reason;
        this.affectedBookings = affectedBookings;
    }
}