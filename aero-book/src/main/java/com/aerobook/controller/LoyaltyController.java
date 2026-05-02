package com.aerobook.controller;


import com.aerobook.constants.ApiConstants;
import com.aerobook.domain.dto.request.MileAdjustRequest;
import com.aerobook.domain.dto.request.MileRedeemRequest;
import com.aerobook.domain.dto.response.LoyaltyAccountResponse;
import com.aerobook.domain.dto.response.MileTransactionResponse;
import com.aerobook.security.UserPrincipal;
import com.aerobook.service.LoyaltyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * The type Loyalty controller.
 */
@RestController
@RequestMapping(ApiConstants.LOYALTY)
@RequiredArgsConstructor
public class LoyaltyController {

    private final LoyaltyService loyaltyService;

    /**
     * Gets my account.
     *
     * @param principal the principal
     * @return the my account
     */
    @GetMapping("/my-account")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LoyaltyAccountResponse> getMyAccount(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(
                loyaltyService.getMyAccount(principal.getId()));
    }

    /**
     * Gets my transactions.
     *
     * @param principal the principal
     * @param pageable  the pageable
     * @return the my transactions
     */
    @GetMapping("/my-transactions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MileTransactionResponse>> getMyTransactions(
            @AuthenticationPrincipal UserPrincipal principal,
            Pageable pageable) {
        return ResponseEntity.ok(
                loyaltyService.getTransactions(principal.getId(), pageable));
    }

    /**
     * Gets account by user.
     *
     * @param userId the user id
     * @return the account by user
     */
    @GetMapping("/account/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<LoyaltyAccountResponse> getAccountByUser(
            @PathVariable Long userId) {
        return ResponseEntity.ok(loyaltyService.getAccountByUser(userId));
    }

    /**
     * Gets transactions.
     *
     * @param userId   the user id
     * @param pageable the pageable
     * @return the transactions
     */
    @GetMapping("/transactions/{userId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AGENT')")
    public ResponseEntity<List<MileTransactionResponse>> getTransactions(
            @PathVariable Long userId,
            Pageable pageable) {
        return ResponseEntity.ok(
                loyaltyService.getTransactions(userId, pageable));
    }

    /**
     * Create account response entity.
     *
     * @param userId the user id
     * @return the response entity
     */
    @PostMapping("/account/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<LoyaltyAccountResponse> createAccount(
            @PathVariable Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(loyaltyService.createAccountForUser(userId));
    }

    /**
     * Redeem miles response entity.
     *
     * @param principal the principal
     * @param request   the request
     * @return the response entity
     */
    @PostMapping("/redeem")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LoyaltyAccountResponse> redeemMiles(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody MileRedeemRequest request) {
        return ResponseEntity.ok(
                loyaltyService.redeemMiles(principal.getId(), request));
    }

    /**
     * Adjust miles response entity.
     *
     * @param userId  the user id
     * @param request the request
     * @return the response entity
     */
    @PostMapping("/adjust/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<LoyaltyAccountResponse> adjustMiles(
            @PathVariable Long userId,
            @Valid @RequestBody MileAdjustRequest request) {
        return ResponseEntity.ok(
                loyaltyService.adjustMiles(userId, request));
    }
}