package com.aerobook.entity;


import com.aerobook.domain.enums.MileTransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "mile_transactions")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MileTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loyalty_account_id", nullable = false)
    private LoyaltyAccount loyaltyAccount;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private MileTransactionType type;

    @Column(name = "miles", nullable = false)
    private Long miles;                     // positive = earned, negative = redeemed

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "reference_id")
    private String referenceId;             // booking PNR, flight number etc.

    @Column(name = "balance_after", nullable = false)
    private Long balanceAfter;              // available miles after transaction

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}