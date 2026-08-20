package com.booking_ticket_platform.concert.entity;

import com.booking_ticket_platform.venue.entity.Venue;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.List;

@Entity
@Table(name = "concerts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Concert {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    @Column(nullable = false)
    private String name;

    @Column(name = "event_date", nullable = false)
    private OffsetDateTime eventDate;

    // e.g., DRAFT, PUBLISHED, CANCELLED
    @Column(nullable = false)
    private String status;
    
    @Column(name = "stage_layout")
    @Builder.Default
    private String stageLayout = "FRONT";

    @OneToMany(mappedBy = "concert", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TicketCategory> ticketCategories;
}
