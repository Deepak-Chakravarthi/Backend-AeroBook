package com.aerobook.controller;


import com.aerobook.domain.dto.response.NotificationResponse;
import com.aerobook.security.UserPrincipal;
import com.aerobook.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // ── Own notifications ─────────────────────────────────────────────
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(
            @AuthenticationPrincipal UserPrincipal principal,
            Pageable pageable) {
        return ResponseEntity.ok(notificationService
                .getNotificationsByUser(principal.getId(), pageable));
    }

    // ── Admin — get by user ───────────────────────────────────────────
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByUser(
            @PathVariable Long userId,
            Pageable pageable) {
        return ResponseEntity.ok(notificationService
                .getNotificationsByUser(userId, pageable));
    }
}