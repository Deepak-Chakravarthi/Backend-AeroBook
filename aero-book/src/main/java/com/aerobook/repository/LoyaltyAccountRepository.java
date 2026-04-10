package com.aerobook.repository;


import com.aerobook.entity.LoyaltyAccount;
import com.aerobook.domain.enums.LoyaltyTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LoyaltyAccountRepository
        extends JpaRepository<LoyaltyAccount, Long> {

    Optional<LoyaltyAccount> findByUserId(Long userId);

    Optional<LoyaltyAccount> findByMembershipNumber(String membershipNumber);

    boolean existsByUserId(Long userId);

    List<LoyaltyAccount> findAllByTier(LoyaltyTier tier);

    @Query("SELECT la FROM LoyaltyAccount la " +
            "JOIN FETCH la.user " +
            "WHERE la.user.id = :userId")
    Optional<LoyaltyAccount> findByUserIdWithDetails(Long userId);

    @Query("SELECT la FROM LoyaltyAccount la " +
            "ORDER BY la.totalMiles DESC")
    List<LoyaltyAccount> findTopEarners();
}