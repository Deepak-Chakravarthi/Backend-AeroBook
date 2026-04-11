package com.aerobook.repository;

import com.aerobook.entity.Aircraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

/**
 * The interface Aircraft repository.
 */
public interface AircraftRepository extends JpaRepository<Aircraft, Long>,
        JpaSpecificationExecutor<Aircraft> {

    /**
     * Exists by registration number boolean.
     *
     * @param registrationNumber the registration number
     * @return the boolean
     */
    boolean existsByRegistrationNumber(String registrationNumber);

    /**
     * Find by id with seat configs optional.
     *
     * @param id the id
     * @return the optional
     */
    @Query("SELECT a FROM Aircraft a JOIN FETCH a.seatConfigs WHERE a.id = :id")
    Optional<Aircraft> findByIdWithSeatConfigs(Long id);

}
