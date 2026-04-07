package com.aerobook.repository;


import com.aerobook.enitity.Payment;
import com.aerobook.domain.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long>,
        JpaSpecificationExecutor<Payment> {

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    Optional<Payment> findByPaymentReference(String paymentReference);

    boolean existsByIdempotencyKey(String idempotencyKey);

    List<Payment> findAllByBookingId(Long bookingId);

    @Query("SELECT p FROM Payment p " +
            "JOIN FETCH p.booking b " +
            "JOIN FETCH p.user " +
            "LEFT JOIN FETCH p.refunds " +
            "WHERE p.id = :id")
    Optional<Payment> findByIdWithDetails(Long id);

    @Query("SELECT p FROM Payment p " +
            "JOIN FETCH p.booking b " +
            "WHERE b.id = :bookingId " +
            "AND p.status = :status")
    Optional<Payment> findByBookingIdAndStatus(Long bookingId,
                                               PaymentStatus status);
}