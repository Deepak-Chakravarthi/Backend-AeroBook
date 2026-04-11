package com.aerobook.repository;


import com.aerobook.entity.Notification;
import com.aerobook.domain.enums.NotificationStatus;
import com.aerobook.domain.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    Page<Notification> findAllByUserId(Long userId, Pageable pageable);

    List<Notification> findAllByStatus(NotificationStatus status);

    List<Notification> findAllByStatusAndRetryCountLessThan(
            NotificationStatus status, int maxRetries);

    @Query("SELECT n FROM Notification n " +
            "WHERE n.status = 'FAILED' " +
            "AND n.retryCount < :maxRetries " +
            "ORDER BY n.createdAt ASC")
    List<Notification> findRetryableNotifications(int maxRetries);
}