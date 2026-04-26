package com.aerobook.controller;


import com.aerobook.constants.ApiConstants;
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

/**
 * The type Notification controller.
 */
@RestController
@RequestMapping(ApiConstants.NOTIFICATIONS)
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Gets my notifications.
     *
     * @param principal the principal
     * @param pageable  the pageable
     * @return the my notifications
     */
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(
            @AuthenticationPrincipal UserPrincipal principal,
            Pageable pageable) {
        return ResponseEntity.ok(notificationService
                .getNotificationsByUser(principal.getId(), pageable));
    }

    /**
     * Gets notifications by user.
     *
     * @param userId   the user id
     * @param pageable the pageable
     * @return the notifications by user
     */

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByUser(
            @PathVariable Long userId,
            Pageable pageable) {
        return ResponseEntity.ok(notificationService
                .getNotificationsByUser(userId, pageable));
    }
}