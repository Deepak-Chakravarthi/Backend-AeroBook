package com.aerobook.service;


import com.aerobook.domain.dto.request.MileAdjustRequest;
import com.aerobook.domain.dto.request.MileRedeemRequest;
import com.aerobook.domain.dto.response.LoyaltyAccountResponse;
import com.aerobook.domain.dto.response.MileTransactionResponse;
import com.aerobook.domain.enums.LoyaltyTier;
import com.aerobook.domain.enums.MileTransactionType;
import com.aerobook.domain.enums.SeatClass;
import com.aerobook.entity.Flight;
import com.aerobook.entity.LoyaltyAccount;
import com.aerobook.entity.MileTransaction;
import com.aerobook.entity.User;
import com.aerobook.event.TierUpgradedEvent;
import com.aerobook.exception.AeroBookException;
import com.aerobook.exception.ResourceNotFoundException;
import com.aerobook.mapper.LoyaltyMapper;
import com.aerobook.repository.LoyaltyAccountRepository;
import com.aerobook.repository.MileTransactionRepository;
import com.aerobook.util.MembershipNumberGenerator;
import com.aerobook.util.MilesCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoyaltyService {

    private final LoyaltyAccountRepository loyaltyAccountRepository;
    private final MileTransactionRepository mileTransactionRepository;
    private final LoyaltyMapper loyaltyMapper;
    private final MilesCalculator milesCalculator;
    private final MembershipNumberGenerator membershipNumberGenerator;
    private final ApplicationEventPublisher eventPublisher;
    private final UserService userService;

    // ----------------------------------------------------------------
    // Get account by user id
    // ----------------------------------------------------------------
    public LoyaltyAccountResponse getAccountByUser(Long userId) {
        return loyaltyMapper.toResponse(findAccountByUserId(userId));
    }

    // ----------------------------------------------------------------
    // Get own account
    // ----------------------------------------------------------------
    public LoyaltyAccountResponse getMyAccount(Long currentUserId) {
        LoyaltyAccount account = loyaltyAccountRepository
                .findByUserIdWithDetails(currentUserId)
                .orElseGet(() -> createAccount(currentUserId));
        return loyaltyMapper.toResponse(account);
    }

    // ----------------------------------------------------------------
    // Get transactions
    // ----------------------------------------------------------------
    public List<MileTransactionResponse> getTransactions(Long userId,
                                                         Pageable pageable) {
        LoyaltyAccount account = findAccountByUserId(userId);
        return mileTransactionRepository
                .findAllByLoyaltyAccountId(account.getId(), pageable)
                .map(loyaltyMapper::toTransactionResponse)
                .getContent();
    }

    // ----------------------------------------------------------------
    // Create account — on user registration
    // ----------------------------------------------------------------
    @Transactional
    public LoyaltyAccountResponse createAccountForUser(Long userId) {
        if (loyaltyAccountRepository.existsByUserId(userId)) {
            throw new AeroBookException(
                    "Loyalty account already exists for user: " + userId,
                    HttpStatus.CONFLICT,
                    "LOYALTY_ACCOUNT_EXISTS"
            );
        }
        LoyaltyAccount account = createAccount(userId);
        return loyaltyMapper.toResponse(account);
    }

    // ----------------------------------------------------------------
    // Award miles on flight completion — called by event listener
    // ----------------------------------------------------------------
    @Transactional
    public void awardMilesForFlight(Long userId, Flight flight,
                                    SeatClass seatClass) {
        LoyaltyAccount account = loyaltyAccountRepository
                .findByUserId(userId)
                .orElseGet(() -> createAccount(userId));

        int distanceKm = flight.getRoute().getDistanceKm() != null
                ? flight.getRoute().getDistanceKm() : 500;

        long miles = milesCalculator.calculateEarnedMiles(
                distanceKm, seatClass, account.getTier());

        account.earnMiles(miles);
        account.incrementFlightsCompleted();

        // Record transaction
        recordTransaction(account, MileTransactionType.EARNED, miles,
                "Miles earned for flight " + flight.getFlightNumber()
                        + " (" + seatClass + " class, " + distanceKm + "km)",
                flight.getFlightNumber());

        // Check tier upgrade
        checkAndUpgradeTier(account);

        loyaltyAccountRepository.save(account);

        log.info("Awarded {} miles to user {} for flight {}",
                miles, userId, flight.getFlightNumber());
    }

    // ----------------------------------------------------------------
    // Redeem miles
    // ----------------------------------------------------------------
    @Transactional
    public LoyaltyAccountResponse redeemMiles(Long userId,
                                              MileRedeemRequest request) {
        LoyaltyAccount account = findAccountByUserId(userId);
        account.redeemMiles(request.miles());

        recordTransaction(account, MileTransactionType.REDEEMED,
                -request.miles(),
                request.description() != null
                        ? request.description() : "Miles redeemed",
                null);

        loyaltyAccountRepository.save(account);
        log.info("Redeemed {} miles from user {}", request.miles(), userId);

        return loyaltyMapper.toResponse(account);
    }

    // ----------------------------------------------------------------
    // Admin — adjust miles manually
    // ----------------------------------------------------------------
    @Transactional
    public LoyaltyAccountResponse adjustMiles(Long userId,
                                              MileAdjustRequest request) {
        LoyaltyAccount account = findAccountByUserId(userId);
        account.adjustMiles(request.miles());

        recordTransaction(account, MileTransactionType.ADJUSTED,
                request.miles(), request.description(),
                request.referenceId());

        loyaltyAccountRepository.save(account);
        log.info("Adjusted {} miles for user {}", request.miles(), userId);

        return loyaltyMapper.toResponse(account);
    }

    // ----------------------------------------------------------------
    // Internal — find account
    // ----------------------------------------------------------------
    public LoyaltyAccount findAccountByUserId(Long userId) {
        return loyaltyAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "LoyaltyAccount", "userId", String.valueOf(userId)));
    }

    // ----------------------------------------------------------------
    // Private helpers
    // ----------------------------------------------------------------

    private LoyaltyAccount createAccount(Long userId) {
        User user = userService.findUserById(userId);

        String membershipNumber;
        do {
            membershipNumber = membershipNumberGenerator.generate();
        } while (loyaltyAccountRepository
                .findByMembershipNumber(membershipNumber).isPresent());

        LoyaltyAccount account = LoyaltyAccount.builder()
                .user(user)
                .membershipNumber(membershipNumber)
                .tier(LoyaltyTier.BLUE)
                .totalMiles(0L)
                .availableMiles(0L)
                .tierQualifyingMiles(0L)
                .flightsCompleted(0)
                .lastActivityAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        LoyaltyAccount saved = loyaltyAccountRepository.save(account);
        log.info("Loyalty account created — membership: {}, user: {}",
                membershipNumber, userId);

        return saved;
    }

    private void checkAndUpgradeTier(LoyaltyAccount account) {
        if (!account.isTierUpgradeEligible()) return;

        LoyaltyTier previousTier = account.getTier();
        LoyaltyTier newTier = account.calculateTier();

        account.setTier(newTier);
        account.setTierUpgradedAt(LocalDateTime.now());

        // Award bonus miles on tier upgrade
        long bonusMiles = milesCalculator.calculateTierUpgradeBonus(newTier);
        if (bonusMiles > 0) {
            account.earnMiles(bonusMiles);
            recordTransaction(account, MileTransactionType.BONUS,
                    bonusMiles,
                    "Tier upgrade bonus — welcome to " + newTier + "!",
                    null);
        }

        // Publish tier upgrade event
        eventPublisher.publishEvent(
                new TierUpgradedEvent(this, account, previousTier, newTier));

        log.info("Tier upgraded — user: {}, {} → {}",
                account.getUser().getId(), previousTier, newTier);
    }

    private void recordTransaction(LoyaltyAccount account,
                                   MileTransactionType type,
                                   long miles,
                                   String description,
                                   String referenceId) {
        MileTransaction transaction = MileTransaction.builder()
                .loyaltyAccount(account)
                .type(type)
                .miles(miles)
                .description(description)
                .referenceId(referenceId)
                .balanceAfter(account.getAvailableMiles())
                .createdAt(LocalDateTime.now())
                .build();

        mileTransactionRepository.save(transaction);
    }
}