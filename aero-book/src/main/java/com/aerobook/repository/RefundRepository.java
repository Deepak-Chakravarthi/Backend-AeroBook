package com.aerobook.repository;


import com.aerobook.enitity.Refund;
import com.aerobook.domain.enums.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RefundRepository extends JpaRepository<Refund, Long> {

    Optional<Refund> findByRefundReference(String refundReference);

    boolean existsByRefundReference(String refundReference);

    List<Refund> findAllByPaymentId(Long paymentId);

    List<Refund> findAllByBookingId(Long bookingId);

    @Query("SELECT r FROM Refund r " +
            "JOIN FETCH r.payment p " +
            "JOIN FETCH r.booking b " +
            "WHERE r.id = :id")
    Optional<Refund> findByIdWithDetails(Long id);

    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM Refund r " +
            "WHERE r.payment.id = :paymentId " +
            "AND r.status = 'SUCCESS'")
    java.math.BigDecimal sumRefundedAmountByPayment(Long paymentId);
}