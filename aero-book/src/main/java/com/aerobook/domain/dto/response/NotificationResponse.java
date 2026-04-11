package com.aerobook.domain.dto.response;


import com.aerobook.domain.enums.NotificationChannel;
import com.aerobook.domain.enums.NotificationStatus;
import com.aerobook.domain.enums.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long                id,
        Long                userId,
        NotificationType    type,
        NotificationChannel channel,
        NotificationStatus  status,
        String              recipient,
        String              subject,
        String              referenceId,
        Integer             retryCount,
        String              failureReason,
        LocalDateTime       sentAt,
        LocalDateTime       createdAt
) {}