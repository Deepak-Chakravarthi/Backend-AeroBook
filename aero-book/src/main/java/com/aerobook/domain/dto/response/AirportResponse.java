package com.aerobook.domain.dto.response;


public record AirportResponse(
        Long id,
        String iataCode,
        String name,
        String city,
        String country,
        String timezone,
        Double latitude,
        Double longitude
) {
}