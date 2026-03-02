package com.aerobook.service;

import com.aerobook.enitity.Aircraft;
import com.aerobook.enitity.AircraftSeatConfig;
import com.aerobook.domain.dto.request.AircraftSeatConfigRequest;
import com.aerobook.domain.dto.response.AircraftSeatConfigResponse;
import com.aerobook.exception.DuplicateResourceException;
import com.aerobook.exception.ResourceNotFoundException;
import com.aerobook.mapper.AircraftSeatConfigMapper;
import com.aerobook.repository.AircraftSeatConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AircraftSeatConfigService {

    private final AircraftSeatConfigRepository seatConfigRepository;
    private final AircraftSeatConfigMapper seatConfigMapper;
    private final AircraftService aircraftService;

    public List<AircraftSeatConfigResponse> getSeatConfigsByAircraft(Long aircraftId) {
        return seatConfigRepository.findAllByAircraftId(aircraftId).stream()
                .map(seatConfigMapper::toResponse)
                .toList();
    }

    @Transactional
    public AircraftSeatConfigResponse addSeatConfig(Long aircraftId, AircraftSeatConfigRequest request) {
        Aircraft aircraft = aircraftService.findAircraftById(aircraftId);
        if (seatConfigRepository.existsByAircraftIdAndSeatClass(aircraftId, request.seatClass())) {
            throw new DuplicateResourceException("SeatConfig", "seat class", request.seatClass().name());
        }
        AircraftSeatConfig config = seatConfigMapper.toEntity(request);
        config.setAircraft(aircraft);
        return seatConfigMapper.toResponse(seatConfigRepository.save(config));
    }

    @Transactional
    public AircraftSeatConfigResponse updateSeatConfig(Long configId, AircraftSeatConfigRequest request) {
        AircraftSeatConfig config = seatConfigRepository.findById(configId)
                .orElseThrow(() -> new ResourceNotFoundException("SeatConfig", configId));
        config.setSeatCount(request.seatCount());
        config.setRows(request.rows());
        config.setSeatsPerRow(request.seatsPerRow());
        return seatConfigMapper.toResponse(seatConfigRepository.save(config));
    }

    @Transactional
    public void deleteSeatConfig(Long configId) {
        if (!seatConfigRepository.existsById(configId)) {
            throw new ResourceNotFoundException("SeatConfig", configId);
        }
        seatConfigRepository.deleteById(configId);
    }
}