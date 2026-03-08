package com.aerobook.service.query;

import com.aerobook.enitity.Aircraft;
import com.aerobook.exception.ResourceNotFoundException;
import com.aerobook.repository.AircraftRepository;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;


@AllArgsConstructor
@Service
public class AircraftQueryService {

    private final AircraftRepository aircraftRepository;

    @Cacheable(value = "aircraft", key = "#id")
    public Aircraft findAircraftById(Long id) {

        return aircraftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aircraft", "id", id.toString()));
    }

    @Cacheable(value = "aircraftRegistration", key = "#registrationNumber")
    public boolean existsByRegistrationNumber(String registrationNumber) {

        return aircraftRepository.existsByRegistrationNumber(registrationNumber);
    }
}

