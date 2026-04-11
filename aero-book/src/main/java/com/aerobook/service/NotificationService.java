package com.aerobook.service;

import com.aerobook.config.NotificationProperties;
import com.aerobook.entity.Notification;
import com.aerobook.entity.User;
import com.aerobook.domain.dto.response.NotificationResponse;
import com.aerobook.domain.enums.NotificationChannel;
import com.aerobook.domain.enums.NotificationStatus;
import com.aerobook.domain.enums.NotificationType;
import com.aerobook.event.NotificationEvent;
import com.aerobook.mapper.NotificationMapper;
import com.aerobook.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper     notificationMapper;
    private final EmailService           emailService;
    private final NotificationProperties notificationProperties;
    private final UserService            userService;

    private static final int MAX_RETRIES = 3;

    // ----------------------------------------------------------------
    // Get notifications by user
    // ----------------------------------------------------------------
    public List<NotificationResponse> getNotificationsByUser(Long userId,
                                                             Pageable pageable) {
        return notificationRepository.findAllByUserId(userId, pageable)
                .map(notificationMapper::toResponse)
                .getContent();
    }

    // ----------------------------------------------------------------
    // Process notification event — entry point from listeners
    // ----------------------------------------------------------------
    @Async
    @Transactional
    public void processNotification(NotificationEvent event) {
        log.info("Processing notification — type: {}, user: {}",
                event.getType(), event.getUserId());

        NotificationTemplate template = resolveTemplate(event.getType());
        String subject = resolveSubject(event.getType(),
                event.getTemplateVariables());

        Notification notification = Notification.builder()
                .user(event.getUserId() != null
                        ? userService.findUserById(event.getUserId()) : null)
                .type(event.getType())
                .channel(NotificationChannel.EMAIL)
                .status(NotificationStatus.PENDING)
                .recipient(event.getRecipientEmail())
                .subject(subject)
                .referenceId(event.getReferenceId())
                .retryCount(0)
                .createdAt(LocalDateTime.now())
                .build();

        Notification saved = notificationRepository.save(notification);

        sendWithRetry(saved, template.templateName(),
                event.getTemplateVariables());
    }

    // ----------------------------------------------------------------
    // Retry failed notifications — called by scheduler
    // ----------------------------------------------------------------
    @Transactional
    public void retryFailedNotifications() {
        List<Notification> retryable = notificationRepository
                .findRetryableNotifications(MAX_RETRIES);

        retryable.forEach(notification -> {
            log.info("Retrying notification id: {}, attempt: {}",
                    notification.getId(), notification.getRetryCount() + 1);

            NotificationTemplate template =
                    resolveTemplate(notification.getType());

            sendWithRetry(notification, template.templateName(), Map.of(
                    "referenceId", notification.getReferenceId() != null
                            ? notification.getReferenceId() : ""
            ));
        });
    }

    // ----------------------------------------------------------------
    // Private — send with retry + DLQ logic
    // ----------------------------------------------------------------
    private void sendWithRetry(Notification notification,
                               String templateName,
                               Map<String, Object> variables) {
        try {
            emailService.sendEmail(
                    notification.getRecipient(),
                    notification.getSubject(),
                    templateName,
                    variables
            );

            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.save(notification);

            log.info("Notification sent — id: {}, type: {}",
                    notification.getId(), notification.getType());

        } catch (Exception e) {
            notification.setRetryCount(notification.getRetryCount() + 1);
            notification.setFailureReason(e.getMessage());

            if (notification.getRetryCount() >= MAX_RETRIES) {
                // Move to dead letter
                notification.setStatus(NotificationStatus.DEAD_LETTERED);
                log.error("Notification moved to DLQ — id: {}, type: {}, reason: {}",
                        notification.getId(), notification.getType(),
                        e.getMessage());
                publishToDeadLetterQueue(notification);
            } else {
                notification.setStatus(NotificationStatus.FAILED);
                log.warn("Notification failed — id: {}, retry: {}/{}",
                        notification.getId(), notification.getRetryCount(),
                        MAX_RETRIES);
            }

            notificationRepository.save(notification);
        }
    }

    // ----------------------------------------------------------------
    // Dead letter queue — Phase 1: log + persist
    //                     Phase 2: publish to Kafka DLQ topic
    // ----------------------------------------------------------------
    private void publishToDeadLetterQueue(Notification notification) {
        log.error("DEAD LETTER — notification id: {}, type: {}, recipient: {}, reason: {}",
                notification.getId(),
                notification.getType(),
                notification.getRecipient(),
                notification.getFailureReason());

        // Phase 2: kafkaTemplate.send(KafkaConfig.TOPIC_DLQ_NOTIFICATIONS, notification)
    }

    // ----------------------------------------------------------------
    // Template resolution
    // ----------------------------------------------------------------
    private record NotificationTemplate(String templateName, String subject) {}

    private NotificationTemplate resolveTemplate(NotificationType type) {
        return switch (type) {
            case BOOKING_CONFIRMED  -> new NotificationTemplate(
                    "booking-confirmed", "Booking Confirmed");
            case BOOKING_CANCELLED  -> new NotificationTemplate(
                    "booking-cancelled", "Booking Cancelled");
            case PAYMENT_SUCCESS    -> new NotificationTemplate(
                    "payment-success", "Payment Successful");
            case PAYMENT_FAILED     -> new NotificationTemplate(
                    "payment-failed", "Payment Failed");
            case REFUND_INITIATED   -> new NotificationTemplate(
                    "refund-initiated", "Refund Initiated");
            case REFUND_SUCCESS     -> new NotificationTemplate(
                    "refund-success", "Refund Successful");
            case FLIGHT_DELAYED     -> new NotificationTemplate(
                    "flight-delayed", "Important: Flight Delay Notice");
            case FLIGHT_CANCELLED   -> new NotificationTemplate(
                    "flight-cancelled", "Important: Flight Cancelled");
            case CHECK_IN_OPEN      -> new NotificationTemplate(
                    "checkin-open", "Check-in Now Open for Your Flight");
            case CHECK_IN_CONFIRMED -> new NotificationTemplate(
                    "checkin-confirmed", "Check-in Confirmed");
            case BOARDING_PASS_ISSUED -> new NotificationTemplate(
                    "boarding-pass-issued", "Your Boarding Pass is Ready");
            case TIER_UPGRADED      -> new NotificationTemplate(
                    "tier-upgraded", "Congratulations! Tier Upgrade");
            case MILES_EARNED       -> new NotificationTemplate(
                    "miles-earned", "Miles Credited to Your Account");
            case WELCOME            -> new NotificationTemplate(
                    "welcome", "Welcome to AeroBook Frequent Flyer Program");
        };
    }

    private String resolveSubject(NotificationType type,
                                  Map<String, Object> variables) {
        return switch (type) {
            case BOOKING_CONFIRMED  -> "Booking Confirmed — "
                    + variables.getOrDefault("pnr", "");
            case FLIGHT_DELAYED     -> "Flight Delay: "
                    + variables.getOrDefault("flightNumber", "");
            case TIER_UPGRADED      -> "Welcome to "
                    + variables.getOrDefault("newTier", "") + " Tier!";
            default                 -> resolveTemplate(type).subject();
        };
    }
}