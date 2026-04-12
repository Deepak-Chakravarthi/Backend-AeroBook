package com.aerobook.domain.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RevenueReportResponse(
        LocalDate      from,
        LocalDate      to,
        BigDecimal     totalRevenue,
        BigDecimal     totalBaseFare,
        BigDecimal     totalTax,
        int            totalBookings,
        int            totalTickets,
        BigDecimal     averageTicketValue,
        BigDecimal     highestSingleBooking,
        List<RevenueByRoute>   revenueByRoute,
        List<RevenueByAirline> revenueByAirline,
        List<RevenueByClass>   revenueByClass,
        List<RevenueTrend>     revenueTrend
) {
    public record RevenueByRoute(
            String     originCode,
            String     destinationCode,
            int        bookings,
            BigDecimal revenue
    ) {}

    public record RevenueByAirline(
            String     airlineName,
            String     iataCode,
            int        bookings,
            BigDecimal revenue
    ) {}

    public record RevenueByClass(
            String     seatClass,
            int        tickets,
            BigDecimal revenue
    ) {}

    public record RevenueTrend(
            LocalDate  date,
            int        bookings,
            BigDecimal revenue
    ) {}
}