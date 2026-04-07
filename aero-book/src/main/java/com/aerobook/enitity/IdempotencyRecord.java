package com.aerobook.enitity;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Stores processed idempotency keys to prevent duplicate payments.
 * If the same key is submitted again, the original response is returned.
 */
@Entity
@Table(name = "idempotency_records")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class IdempotencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Column(name = "response_body", nullable = false, columnDefinition = "TEXT")
    private String responseBody;            // cached JSON response

    @Column(name = "http_status", nullable = false)
    private Integer httpStatus;

    @Column(name = "request_path", nullable = false)
    private String requestPath;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Records expire after 24 hours — handled by scheduler
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}
