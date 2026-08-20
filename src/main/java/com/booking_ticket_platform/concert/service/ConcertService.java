package com.booking_ticket_platform.concert.service;

import com.booking_ticket_platform.concert.dto.ConcertCreateRequest;
import com.booking_ticket_platform.concert.dto.ConcertDTO;
import com.booking_ticket_platform.concert.dto.TicketCategoryCreateRequest;
import com.booking_ticket_platform.concert.dto.TicketCategoryDTO;
import com.booking_ticket_platform.concert.entity.Concert;
import com.booking_ticket_platform.concert.entity.TicketCategory;
import com.booking_ticket_platform.concert.repository.IConcertRepository;
import com.booking_ticket_platform.concert.repository.ITicketCategoryRepository;
import com.booking_ticket_platform.shared.exception.ResourceNotFoundException;
import com.booking_ticket_platform.venue.dto.VenueDTO;
import com.booking_ticket_platform.venue.entity.Venue;
import com.booking_ticket_platform.venue.repository.IVenueRepository;
import com.booking_ticket_platform.concert.repository.ISeatRepository;
import com.booking_ticket_platform.booking.repository.IBookingRepository;
import com.booking_ticket_platform.booking.entity.Booking;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ConcertService implements IConcertService {

    private final IConcertRepository concertRepository;
    private final IVenueRepository venueRepository;
    private final ITicketCategoryRepository ticketCategoryRepository;
    private final ISeatRepository seatRepository;
    private final IBookingRepository bookingRepository;

    public ConcertService(IConcertRepository concertRepository,
                          IVenueRepository venueRepository,
                          ITicketCategoryRepository ticketCategoryRepository,
                          ISeatRepository seatRepository,
                          IBookingRepository bookingRepository) {
        this.concertRepository = concertRepository;
        this.venueRepository = venueRepository;
        this.ticketCategoryRepository = ticketCategoryRepository;
        this.seatRepository = seatRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public ConcertDTO createConcert(ConcertCreateRequest request) {
        Venue venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found"));

        Concert concert = Concert.builder()
                .venue(venue)
                .name(request.getName())
                .eventDate(request.getEventDate())
                .status("DRAFT")
                .ticketCategories(new ArrayList<>())
                .build();

        return mapToDTO(concertRepository.save(concert));
    }

    @Override
    public ConcertDTO updateConcert(UUID id, ConcertCreateRequest request) {
        Concert concert = concertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Concert not found"));
        
        Venue venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found"));

        concert.setVenue(venue);
        concert.setName(request.getName());
        concert.setEventDate(request.getEventDate());
        return mapToDTO(concertRepository.save(concert));
    }

    @Override
    public ConcertDTO publishConcert(UUID id) {
        Concert concert = concertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Concert not found"));
        concert.setStatus("PUBLISHED");
        return mapToDTO(concertRepository.save(concert));
    }

    @Override
    @Transactional
    public ConcertDTO cancelConcert(UUID id) {
        Concert concert = concertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Concert not found"));
        
        concert.setStatus("CANCELLED");
        
        // Find all completed bookings and refund them
        List<Booking> completedBookings = bookingRepository.findByConcertIdAndStatus(id, "COMPLETED");
        for (Booking booking : completedBookings) {
            booking.setStatus("REFUNDED");
        }
        bookingRepository.saveAll(completedBookings);
        
        return mapToDTO(concertRepository.save(concert));
    }

    @Override
    public TicketCategoryDTO addTicketCategory(UUID concertId, TicketCategoryCreateRequest request) {
        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new ResourceNotFoundException("Concert not found"));

        TicketCategory category = TicketCategory.builder()
                .concert(concert)
                .name(request.getName())
                .type(request.getType())
                .price(request.getPrice())
                .totalQuantity(request.getTotalQuantity())
                .availableQuantity(request.getTotalQuantity())
                .version(0)
                .build();

        return mapCategoryToDTO(ticketCategoryRepository.save(category));
    }

    @Override
    public List<ConcertDTO> getPublishedConcerts() {
        return concertRepository.findByStatus("PUBLISHED").stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ConcertDTO getConcertById(UUID id) {
        Concert concert = concertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Concert not found"));
        return mapToDTO(concert);
    }

    private ConcertDTO mapToDTO(Concert concert) {
        Venue venue = concert.getVenue();
        VenueDTO venueDTO = VenueDTO.builder()
                .id(venue.getId())
                .name(venue.getName())
                .address(venue.getAddress())
                .city(venue.getCity())
                .capacity(venue.getCapacity())
                .build();

        List<TicketCategoryDTO> categories = new ArrayList<>();
        if (concert.getTicketCategories() != null) {
            categories = concert.getTicketCategories().stream()
                    .map(this::mapCategoryToDTO)
                    .collect(Collectors.toList());
        }

        return ConcertDTO.builder()
                .id(concert.getId())
                .venue(venueDTO)
                .name(concert.getName())
                .eventDate(concert.getEventDate())
                .status(concert.getStatus())
                .ticketCategories(categories)
                .build();
    }

    private TicketCategoryDTO mapCategoryToDTO(TicketCategory category) {
        return TicketCategoryDTO.builder()
                .id(category.getId())
                .concertId(category.getConcert().getId())
                .name(category.getName())
                .type(category.getType())
                .price(category.getPrice())
                .totalQuantity(category.getTotalQuantity())
                .availableQuantity(category.getAvailableQuantity())
                .build();
    }
}
