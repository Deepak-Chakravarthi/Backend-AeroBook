package com.aerobook.domain.dto.request;



import com.aerobook.domain.enums.BookingType;
import com.aerobook.domain.enums.SeatClass;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record BookingRequest(

        @NotNull(message = "Booking type is required")
        BookingType bookingType,

        // ── Outbound ─────────────────────────────────────────────────
        @NotNull(message = "Outbound flight ID is required")
        Long outboundFlightId,

        @NotNull(message = "Outbound seat class is required")
        SeatClass outboundSeatClass,

        String outboundPreferredSeat,       // optional

        // ── Return (required for RETURN bookings) ─────────────────────
        Long returnFlightId,
        SeatClass returnSeatClass,
        String returnPreferredSeat,

        // ── Passenger details ─────────────────────────────────────────
        @NotBlank(message = "First name is required")
        String passengerFirstName,

        @NotBlank(message = "Last name is required")
        String passengerLastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String passengerEmail,

        @NotBlank(message = "Phone is required")
        String passengerPhone,

        LocalDate passengerDob,
        String passportNumber,
        String nationality
) {
    public void validate() {
        if (bookingType == BookingType.RETURN) {
            if (returnFlightId == null) {
                throw new IllegalArgumentException(
                        "returnFlightId is required for RETURN booking");
            }
            if (returnSeatClass == null) {
                throw new IllegalArgumentException(
                        "returnSeatClass is required for RETURN booking");
            }
        }
    }
}