package com.aerobook.repository;

import com.aerobook.enitity.Aircraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AircraftRepository extends JpaRepository<Aircraft, Long>,
        JpaSpecificationExecutor<Aircraft> {

    boolean existsByRegistrationNumber(String registrationNumber);

    @Query("SELECT a FROM Aircraft a JOIN FETCH a.seatConfigs WHERE a.id = :id")
    Optional<Aircraft> findByIdWithSeatConfigs(Long id);

    Optional<Aircraft> findBy(Long id);
}
