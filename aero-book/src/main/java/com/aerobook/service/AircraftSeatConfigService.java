package com.aerobook.service;

import com.aerobook.domain.dto.request.AircraftSeatConfigRequest;
import com.aerobook.domain.dto.response.AircraftSeatConfigResponse;
import com.aerobook.enitity.Aircraft;
import com.aerobook.enitity.AircraftSeatConfig;
import com.aerobook.exception.DuplicateResourceException;
import com.aerobook.exception.ResourceNotFoundException;
import com.aerobook.mapper.AircraftSeatConfigMapper;
import com.aerobook.repository.AircraftSeatConfigRepository;
import com.aerobook.service.query.AircraftQueryService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * The type Aircraft seat config service.
 */
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class AircraftSeatConfigService {

    private final AircraftSeatConfigRepository seatConfigRepository;
    private final AircraftSeatConfigMapper seatConfigMapper;
    private final AircraftQueryService aircraftQueryService;

    /**
     * Gets seat configs by aircraft.
     *
     * @param aircraftId the aircraft id
     * @return the seat configs by aircraft
     */
    public List<AircraftSeatConfigResponse> getSeatConfigsByAircraft(Long aircraftId) {
        return seatConfigRepository.findAllByAircraftId(aircraftId).stream()
                .map(seatConfigMapper::toResponse)
                .toList();
    }

    /**
     * Add seat config aircraft seat config response.
     *
     * @param aircraftId the aircraft id
     * @param request    the request
     * @return the aircraft seat config response
     */
    @Transactional
    public AircraftSeatConfigResponse addSeatConfig(Long aircraftId, AircraftSeatConfigRequest request) {
        Aircraft aircraft = aircraftQueryService.findAircraftById(aircraftId);
        if (seatConfigRepository.existsByAircraftIdAndSeatClass(aircraftId, request.seatClass())) {
            throw new DuplicateResourceException("SeatConfig", "seat class", request.seatClass().name());
        }
        AircraftSeatConfig config = seatConfigMapper.toEntity(request);
        config.setAircraft(aircraft);
        return seatConfigMapper.toResponse(seatConfigRepository.save(config));
    }

    /**
     * Update seat config aircraft seat config response.
     *
     * @param configId the config id
     * @param request  the request
     * @return the aircraft seat config response
     */
    @Transactional
    public AircraftSeatConfigResponse updateSeatConfig(Long configId, AircraftSeatConfigRequest request) {
        AircraftSeatConfig config = seatConfigRepository.findById(configId)
                .orElseThrow(() -> new ResourceNotFoundException("SeatConfig", configId));
        config.setSeatCount(request.seatCount());
        config.setRows(request.rows());
        config.setSeatsPerRow(request.seatsPerRow());
        return seatConfigMapper.toResponse(seatConfigRepository.save(config));
    }

    /**
     * Delete seat config.
     *
     * @param configId the config id
     */
    @Transactional
    public void deleteSeatConfig(Long configId) {
        if (!seatConfigRepository.existsById(configId)) {
            throw new ResourceNotFoundException("SeatConfig", configId);
        }
        seatConfigRepository.deleteById(configId);
    }
}