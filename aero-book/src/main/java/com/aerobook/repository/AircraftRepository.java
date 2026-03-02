package com.aerobook.repository;

import com.aerobook.domain.enums.AircraftStatus;
import com.aerobook.enitity.Aircraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AircraftRepository extends JpaRepository<Aircraft, Long> {
    Optional<Aircraft> findByRegistrationNumber(String registrationNumber);

    boolean existsByRegistrationNumber(String registrationNumber);

    List<Aircraft> findAllByAirlineId(Long airlineId);

    List<Aircraft> findAllByStatus(AircraftStatus status);

    @Query("SELECT a FROM Aircraft a JOIN FETCH a.seatConfigs WHERE a.id = :id")
    Optional<Aircraft> findByIdWithSeatConfigs(Long id);
}
