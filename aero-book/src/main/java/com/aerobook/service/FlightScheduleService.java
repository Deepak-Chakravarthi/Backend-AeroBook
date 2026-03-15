package com.aerobook.service;


import com.aerobook.domain.dto.request.FlightRequest;
import com.aerobook.domain.dto.request.FlightScheduleRequest;
import com.aerobook.domain.dto.response.FlightScheduleResponse;
import com.aerobook.domain.enums.FlightStatus;
import com.aerobook.enitity.Aircraft;
import com.aerobook.enitity.Airline;
import com.aerobook.enitity.FlightSchedule;
import com.aerobook.enitity.Route;
import com.aerobook.exception.ResourceNotFoundException;
import com.aerobook.mapper.FlightMapper;
import com.aerobook.repository.FlightScheduleRepository;
import com.aerobook.service.query.AircraftQueryService;
import com.aerobook.service.query.AirlineQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FlightScheduleService {

    private final FlightScheduleRepository scheduleRepository;
    private final FlightMapper flightMapper;
    private final FlightService flightService;
    private final AirlineQueryService airlineQueryService;
    private final AircraftQueryService aircraftQueryService;
    private final RouteService routeService;

    public List<FlightScheduleResponse> getAllActiveSchedules() {
        return scheduleRepository.findAllActiveWithDetails()
                .stream()
                .map(flightMapper::scheduleToResponse)
                .toList();
    }

    public FlightScheduleResponse getScheduleById(Long id) {
        return flightMapper.scheduleToResponse(findScheduleById(id));
    }

    @Transactional
    public FlightScheduleResponse createSchedule(FlightScheduleRequest request) {
        Airline airline = airlineQueryService.findAirlineById(request.airlineId());
        Aircraft aircraft = aircraftQueryService.findAircraftById(request.aircraftId());
        Route route = routeService.findRouteById(request.routeId());

        FlightSchedule schedule = flightMapper.scheduleToEntity(request);
        schedule.setAirline(airline);
        schedule.setAircraft(aircraft);
        schedule.setRoute(route);
        schedule.setCreatedAt(LocalDateTime.now());

        return flightMapper.scheduleToResponse(scheduleRepository.save(schedule));
    }

    @Transactional
    public int generateFlightsFromSchedule(Long scheduleId,
                                           LocalDate from,
                                           LocalDate until) {
        FlightSchedule schedule = findScheduleById(scheduleId);
        int count = 0;

        for (LocalDate date = from; !date.isAfter(until); date = date.plusDays(1)) {
            if (isScheduledOnDay(schedule, date)) {
                boolean alreadyExists = flightService.existsByFlightNumberAndDate(
                        schedule.getFlightNumberPrefix(), date);

                if (!alreadyExists) {
                    createFlightFromSchedule(schedule, date);
                    count++;
                }
            }
        }

        log.info("Generated {} flights from schedule {} for {} to {}",
                count, scheduleId, from, until);
        return count;
    }

    @Transactional
    public void deactivateSchedule(Long id) {
        FlightSchedule schedule = findScheduleById(id);
        schedule.setActive(false);
        scheduleRepository.save(schedule);
    }

    @Transactional
    public void deleteSchedule(Long id) {
        if (!scheduleRepository.existsById(id)) {
            throw new ResourceNotFoundException("FlightSchedule", id);
        }
        scheduleRepository.deleteById(id);
    }

    private FlightSchedule findScheduleById(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FlightSchedule", id));
    }

    private boolean isScheduledOnDay(FlightSchedule schedule, LocalDate date) {
        String javaDayName = date.getDayOfWeek().name();   // MONDAY, TUESDAY etc
        return schedule.getOperatingDays().stream()
                .anyMatch(day -> day.name().equals(javaDayName));
    }

    private void createFlightFromSchedule(FlightSchedule schedule, LocalDate date) {
        LocalDateTime departure = LocalDateTime.of(date, schedule.getDepartureTime());
        LocalDateTime arrival = departure.plusMinutes(schedule.getDurationMinutes());

        FlightRequest request = new FlightRequest(
                schedule.getFlightNumberPrefix(),
                schedule.getAirline().getId(),
                schedule.getAircraft().getId(),
                schedule.getRoute().getId(),
                date,
                departure,
                arrival,
                schedule.getDurationMinutes(),
                FlightStatus.SCHEDULED,
                schedule.getGate(),
                schedule.getTerminal()
        );

        flightService.createFlight(request);
    }
}
