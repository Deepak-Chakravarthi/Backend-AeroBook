package com.aerobook.repository;


import com.aerobook.entity.MileTransaction;
import com.aerobook.domain.enums.MileTransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MileTransactionRepository
        extends JpaRepository<MileTransaction, Long> {

    Page<MileTransaction> findAllByLoyaltyAccountId(Long accountId,
                                                    Pageable pageable);

    List<MileTransaction> findAllByLoyaltyAccountIdAndType(
            Long accountId, MileTransactionType type);

    @Query("SELECT COALESCE(SUM(t.miles), 0) FROM MileTransaction t " +
            "WHERE t.loyaltyAccount.id = :accountId " +
            "AND t.type = 'EARNED'")
    Long sumEarnedMilesByAccount(Long accountId);
}