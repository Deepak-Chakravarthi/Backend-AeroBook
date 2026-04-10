package com.aerobook.controller;


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

@RestController
@RequestMapping("/loyalty")
@RequiredArgsConstructor
public class LoyaltyController {

    private final LoyaltyService loyaltyService;

    // ── Own account ───────────────────────────────────────────────────
    @GetMapping("/my-account")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LoyaltyAccountResponse> getMyAccount(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(
                loyaltyService.getMyAccount(principal.getId()));
    }

    // ── Own transactions ──────────────────────────────────────────────
    @GetMapping("/my-transactions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MileTransactionResponse>> getMyTransactions(
            @AuthenticationPrincipal UserPrincipal principal,
            Pageable pageable) {
        return ResponseEntity.ok(
                loyaltyService.getTransactions(principal.getId(), pageable));
    }

    // ── Admin — get account by user id ───────────────────────────────
    @GetMapping("/account/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<LoyaltyAccountResponse> getAccountByUser(
            @PathVariable Long userId) {
        return ResponseEntity.ok(loyaltyService.getAccountByUser(userId));
    }

    // ── Admin — get transactions by user ─────────────────────────────
    @GetMapping("/transactions/{userId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AGENT')")
    public ResponseEntity<List<MileTransactionResponse>> getTransactions(
            @PathVariable Long userId,
            Pageable pageable) {
        return ResponseEntity.ok(
                loyaltyService.getTransactions(userId, pageable));
    }

    // ── Create account manually ───────────────────────────────────────
    @PostMapping("/account/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<LoyaltyAccountResponse> createAccount(
            @PathVariable Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(loyaltyService.createAccountForUser(userId));
    }

    // ── Redeem miles ──────────────────────────────────────────────────
    @PostMapping("/redeem")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LoyaltyAccountResponse> redeemMiles(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody MileRedeemRequest request) {
        return ResponseEntity.ok(
                loyaltyService.redeemMiles(principal.getId(), request));
    }

    // ── Admin — adjust miles ──────────────────────────────────────────
    @PostMapping("/adjust/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<LoyaltyAccountResponse> adjustMiles(
            @PathVariable Long userId,
            @Valid @RequestBody MileAdjustRequest request) {
        return ResponseEntity.ok(
                loyaltyService.adjustMiles(userId, request));
    }
}