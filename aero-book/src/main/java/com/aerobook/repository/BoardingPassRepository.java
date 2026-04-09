package com.aerobook.repository;

import com.aerobook.enitity.BoardingPass;
import com.aerobook.domain.enums.BoardingPassStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BoardingPassRepository extends JpaRepository<BoardingPass, Long> {

    Optional<BoardingPass> findByBoardingPassNumber(String boardingPassNumber);

    boolean existsByBoardingPassNumber(String boardingPassNumber);

    Optional<BoardingPass> findByCheckInId(Long checkInId);

    List<BoardingPass> findAllByFlightId(Long flightId);

    List<BoardingPass> findAllByPassengerId(Long passengerId);

    @Query("SELECT bp FROM BoardingPass bp " +
            "JOIN FETCH bp.checkIn c " +
            "JOIN FETCH bp.passenger p " +
            "JOIN FETCH bp.flight f " +
            "JOIN FETCH f.route r " +
            "JOIN FETCH r.origin " +
            "JOIN FETCH r.destination " +
            "WHERE bp.id = :id")
    Optional<BoardingPass> findByIdWithDetails(Long id);

    @Query("SELECT bp FROM BoardingPass bp " +
            "WHERE bp.flight.id = :flightId " +
            "AND bp.status = :status")
    List<BoardingPass> findAllByFlightIdAndStatus(Long flightId,
                                                  BoardingPassStatus status);
}