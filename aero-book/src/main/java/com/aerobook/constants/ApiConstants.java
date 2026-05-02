package com.aerobook.constants;

/**
 * Central API path constants.
 * All controllers reference these — change version here, applies everywhere.
 */
public final class ApiConstants {

    private ApiConstants() {}

    public static final String API_V1         = "/api/v1";

    // Auth
    public static final String AUTH           = API_V1 + "/auth";

    // Reference data
    public static final String AIRLINES       = API_V1 + "/airlines";
    public static final String AIRCRAFT       = API_V1 + "/aircraft";
    public static final String AIRPORTS       = API_V1 + "/airports";
    public static final String ROUTES         = API_V1 + "/routes";

    // Flights
    public static final String FLIGHTS        = API_V1 + "/flights";
    public static final String FLIGHT_SEARCH  = API_V1 + "/flights/search";
    public static final String SCHEDULES      = API_V1 + "/flight-schedules";

    // Seat
    public static final String SEATS          = API_V1 + "/seats";

    // Booking flow
    public static final String BOOKINGS       = API_V1 + "/bookings";
    public static final String PASSENGERS     = API_V1 + "/passengers";
    public static final String TICKETS        = API_V1 + "/tickets";

    // Payment
    public static final String PAYMENTS       = API_V1 + "/payments";

    // Check-in
    public static final String CHECK_IN       = API_V1 + "/check-in";
    public static final String BOARDING_PASSES = API_V1 + "/boarding-passes";

    // Loyalty
    public static final String LOYALTY        = API_V1 + "/loyalty";

    // Notifications
    public static final String NOTIFICATIONS  = API_V1 + "/notifications";

    // Users
    public static final String USERS          = API_V1 + "/users";

    // Admin
    public static final String ADMIN          = API_V1 + "/admin";
}