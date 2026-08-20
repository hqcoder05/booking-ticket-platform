package com.booking_ticket_platform.payment.entity;

import com.booking_ticket_platform.booking.entity.Booking;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    // e.g., PENDING, COMPLETED, FAILED
    @Column(nullable = false)
    private String status;

    // e.g., CREDIT_CARD, MOMO, ZALOPAY
    @Column(nullable = false)
    private String method;
}
