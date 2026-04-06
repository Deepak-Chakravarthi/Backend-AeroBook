package com.aerobook.enitity;


import com.aerobook.domain.enums.Gender;
import com.aerobook.domain.enums.PassengerType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Passenger — the traveller on a booking.
 * Not necessarily a system User — can be anyone
 * (family member, friend etc.) booked by a User.
 *
 * user_id is nullable — only populated if the passenger
 * is also a registered system User.
 */
@Entity
@Table(name = "passengers")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Passenger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    // Optional link to system User — null if passenger is a third party
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "passport_number")
    private String passportNumber;

    @Column(name = "passport_expiry")
    private LocalDate passportExpiry;

    @Column(name = "nationality")
    private String nationality;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "passenger_type", nullable = false)
    private PassengerType passengerType;

    @OneToMany(mappedBy = "passenger",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<Ticket> tickets = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
