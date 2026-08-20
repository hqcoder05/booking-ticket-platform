package com.booking_ticket_platform.concert.entity;

import com.booking_ticket_platform.booking.entity.Booking;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "seats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_category_id", nullable = false)
    private TicketCategory ticketCategory;

    @Column(name = "seat_number", nullable = false)
    private String seatNumber;

    // e.g., AVAILABLE, HELD, BOOKED
    @Column(nullable = false)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "held_by_booking_id")
    private Booking booking;
}
