package com.booking_ticket_platform.booking.service;

import com.booking_ticket_platform.auth.entity.User;
import com.booking_ticket_platform.auth.repository.UserRepository;
import com.booking_ticket_platform.booking.dto.BookingDTO;
import com.booking_ticket_platform.booking.dto.BookingRequest;
import com.booking_ticket_platform.booking.entity.Booking;
import com.booking_ticket_platform.booking.entity.BookingDetail;
import com.booking_ticket_platform.booking.repository.IBookingDetailRepository;
import com.booking_ticket_platform.booking.repository.IBookingRepository;
import com.booking_ticket_platform.concert.entity.Concert;
import com.booking_ticket_platform.concert.entity.Seat;
import com.booking_ticket_platform.concert.entity.TicketCategory;
import com.booking_ticket_platform.concert.repository.IConcertRepository;
import com.booking_ticket_platform.concert.repository.ISeatRepository;
import com.booking_ticket_platform.concert.repository.ITicketCategoryRepository;
import com.booking_ticket_platform.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BookingService implements IBookingService {

    private final IBookingRepository bookingRepository;
    private final IBookingDetailRepository bookingDetailRepository;
    private final ISeatRepository seatRepository;
    private final ITicketCategoryRepository ticketCategoryRepository;
    private final IConcertRepository concertRepository;
    private final UserRepository userRepository;

    public BookingService(IBookingRepository bookingRepository,
                          IBookingDetailRepository bookingDetailRepository,
                          ISeatRepository seatRepository,
                          ITicketCategoryRepository ticketCategoryRepository,
                          IConcertRepository concertRepository,
                          UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.bookingDetailRepository = bookingDetailRepository;
        this.seatRepository = seatRepository;
        this.ticketCategoryRepository = ticketCategoryRepository;
        this.concertRepository = concertRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public BookingDTO createBooking(UUID userId, BookingRequest request) {
        
        // Idempotency Check
        if (bookingRepository.findByIdempotencyKey(request.getIdempotencyKey()).isPresent()) {
            throw new IllegalStateException("Booking with this idempotency key already exists");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Concert concert = concertRepository.findById(request.getConcertId())
                .orElseThrow(() -> new ResourceNotFoundException("Concert not found"));

        if (!"PUBLISHED".equals(concert.getStatus())) {
            throw new IllegalStateException("Concert is not published for booking");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<BookingDetail> detailsToSave = new ArrayList<>();
        List<Seat> seatsToUpdate = new ArrayList<>();

        // Create booking instance
        Booking booking = Booking.builder()
                .user(user)
                .concert(concert)
                .idempotencyKey(request.getIdempotencyKey())
                .status("PENDING")
                .createdAt(OffsetDateTime.now())
                .totalAmount(BigDecimal.ZERO) // temporary
                .build();

        // 1. Process Seated Tickets (Pessimistic Lock)
        if (request.getSeatIds() != null && !request.getSeatIds().isEmpty()) {
            for (UUID seatId : request.getSeatIds()) {
                Seat seat = seatRepository.findByIdForUpdate(seatId)
                        .orElseThrow(() -> new ResourceNotFoundException("Seat not found: " + seatId));

                if (!"AVAILABLE".equals(seat.getStatus())) {
                    throw new IllegalStateException("Seat is not available: " + seat.getSeatNumber());
                }

                // Hold seat
                seat.setStatus("HELD");
                seat.setBooking(booking);
                seatsToUpdate.add(seat);

                totalAmount = totalAmount.add(seat.getTicketCategory().getPrice());

                BookingDetail detail = BookingDetail.builder()
                        .booking(booking)
                        .ticketCategory(seat.getTicketCategory())
                        .seat(seat)
                        .quantity(1)
                        .price(seat.getTicketCategory().getPrice())
                        .build();
                detailsToSave.add(detail);
            }
        }

        // 2. Process Standing Tickets (Pessimistic Lock)
        if (request.getStandingTickets() != null && !request.getStandingTickets().isEmpty()) {
            for (BookingRequest.StandingTicketRequest standingReq : request.getStandingTickets()) {
                TicketCategory category = ticketCategoryRepository.findByIdForUpdate(standingReq.getTicketCategoryId())
                        .orElseThrow(() -> new ResourceNotFoundException("Ticket Category not found"));

                if (!"STANDING".equals(category.getType())) {
                    throw new IllegalStateException("Ticket category is not standing: " + category.getName());
                }

                if (category.getAvailableQuantity() < standingReq.getQuantity()) {
                    throw new IllegalStateException("Not enough tickets available for category: " + category.getName());
                }

                category.setAvailableQuantity(category.getAvailableQuantity() - standingReq.getQuantity());
                ticketCategoryRepository.save(category);

                totalAmount = totalAmount.add(category.getPrice().multiply(BigDecimal.valueOf(standingReq.getQuantity())));

                BookingDetail detail = BookingDetail.builder()
                        .booking(booking)
                        .ticketCategory(category)
                        .quantity(standingReq.getQuantity())
                        .price(category.getPrice())
                        .build();
                detailsToSave.add(detail);
            }
        }

        if (totalAmount.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Booking must contain at least one ticket");
        }

        booking.setTotalAmount(totalAmount);
        
        Booking savedBooking = bookingRepository.save(booking);
        
        if (!detailsToSave.isEmpty()) {
            bookingDetailRepository.saveAll(detailsToSave);
        }
        
        if (!seatsToUpdate.isEmpty()) {
            seatRepository.saveAll(seatsToUpdate);
        }

        return BookingDTO.builder()
                .id(savedBooking.getId())
                .userId(savedBooking.getUser().getId())
                .concertId(savedBooking.getConcert().getId())
                .status(savedBooking.getStatus())
                .totalAmount(savedBooking.getTotalAmount())
                .createdAt(savedBooking.getCreatedAt())
                .build();
    }

    @Override
    public List<BookingDTO> getMyBookings(UUID userId) {
        return bookingRepository.findByUserId(userId).stream()
                .map(b -> BookingDTO.builder()
                        .id(b.getId())
                        .userId(b.getUser().getId())
                        .concertId(b.getConcert().getId())
                        .status(b.getStatus())
                        .totalAmount(b.getTotalAmount())
                        .createdAt(b.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
