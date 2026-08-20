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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Comparator;

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
                .stageLayout(request.getStageLayout() != null ? request.getStageLayout() : "FRONT")
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
        if (request.getStageLayout() != null) {
            concert.setStageLayout(request.getStageLayout());
        }
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
    @Transactional
    public void deleteConcert(UUID id) {
        Concert concert = concertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Concert not found"));
        
        if (!"DRAFT".equals(concert.getStatus())) {
            throw new IllegalStateException("Only DRAFT concerts can be deleted. Consider cancelling it instead.");
        }
        
        concertRepository.delete(concert);
    }

    @Override
    @Transactional
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
                
        category = ticketCategoryRepository.save(category);
        regenerateAllSeats(concertId);

        return mapCategoryToDTO(category);
    }

    @Override
    @Transactional
    public TicketCategoryDTO updateTicketCategory(UUID concertId, UUID categoryId, TicketCategoryCreateRequest request) {
        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new ResourceNotFoundException("Concert not found"));
        if (!"DRAFT".equals(concert.getStatus())) {
            throw new IllegalStateException("Only DRAFT concerts can be modified.");
        }
        TicketCategory category = ticketCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket category not found"));

        category.setName(request.getName());
        category.setType(request.getType());
        category.setPrice(request.getPrice());
        category.setTotalQuantity(request.getTotalQuantity());
        category.setAvailableQuantity(request.getTotalQuantity());
        category = ticketCategoryRepository.save(category);
        
        regenerateAllSeats(concertId);
        return mapCategoryToDTO(category);
    }

    @Override
    @Transactional
    public void deleteTicketCategory(UUID concertId, UUID categoryId) {
        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new ResourceNotFoundException("Concert not found"));
        if (!"DRAFT".equals(concert.getStatus())) {
            throw new IllegalStateException("Only DRAFT concerts can be modified.");
        }
        TicketCategory category = ticketCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket category not found"));

        ticketCategoryRepository.delete(category);
        ticketCategoryRepository.flush();
        regenerateAllSeats(concertId);
    }

    private void regenerateAllSeats(UUID concertId) {
        seatRepository.deleteAll(seatRepository.findByTicketCategory_Concert_Id(concertId));
        seatRepository.flush();
        
        List<TicketCategory> categories = ticketCategoryRepository.findByConcertId(concertId);
        categories.sort((c1, c2) -> {
            if ("VIP".equalsIgnoreCase(c1.getName()) && !"VIP".equalsIgnoreCase(c2.getName())) {
                return -1;
            } else if (!"VIP".equalsIgnoreCase(c1.getName()) && "VIP".equalsIgnoreCase(c2.getName())) {
                return 1;
            } else {
                return c1.getName().compareToIgnoreCase(c2.getName());
            }
        });
        
        int maxRowIndex = -1;
        
        for (TicketCategory category : categories) {
            if ("SEATED".equalsIgnoreCase(category.getType())) {
                int startRowOffset = maxRowIndex + 1;
                List<com.booking_ticket_platform.concert.entity.Seat> seats = new ArrayList<>();
                int total = category.getTotalQuantity();
                int cols = 20;
                int rowsCount = (int) Math.ceil((double) total / cols);
                if (rowsCount < 1) rowsCount = 1;
                
                int count = 0;
                for (int r = 0; r < rowsCount; r++) {
                    int currentRow = startRowOffset + r;
                    String rowStr = "";
                    int temp = currentRow;
                    do {
                        rowStr = (char) ('A' + (temp % 26)) + rowStr;
                        temp = (temp / 26) - 1;
                    } while (temp >= 0);

                    for (int c = 1; c <= cols; c++) {
                        if (count >= total) break;
                        
                        com.booking_ticket_platform.concert.entity.Seat seat = com.booking_ticket_platform.concert.entity.Seat.builder()
                                .ticketCategory(category)
                                .seatNumber(rowStr + String.valueOf(c))
                                .status("AVAILABLE")
                                .build();
                        seats.add(seat);
                        count++;
                    }
                }
                seatRepository.saveAll(seats);
                seatRepository.flush();
                maxRowIndex += rowsCount;
            }
        }
    }

    @Override
    public List<ConcertDTO> getPublishedConcerts() {
        return concertRepository.findByStatus("PUBLISHED").stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ConcertDTO> getAllConcerts() {
        return concertRepository.findAll().stream()
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
                .stageLayout(concert.getStageLayout())
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

    @Override
    public java.util.List<com.booking_ticket_platform.concert.dto.SeatDTO> getConcertSeats(java.util.UUID concertId) {
        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new com.booking_ticket_platform.shared.exception.ResourceNotFoundException("Concert not found"));
        
        java.util.List<com.booking_ticket_platform.concert.dto.SeatDTO> seatDTOs = new java.util.ArrayList<>();
        for (TicketCategory category : concert.getTicketCategories()) {
            if ("SEATED".equals(category.getType())) {
                java.util.List<com.booking_ticket_platform.concert.entity.Seat> seats = seatRepository.findByTicketCategoryId(category.getId());
                for (com.booking_ticket_platform.concert.entity.Seat seat : seats) {
                    seatDTOs.add(com.booking_ticket_platform.concert.dto.SeatDTO.builder()
                            .id(seat.getId())
                            .ticketCategoryId(category.getId())
                            .seatNumber(seat.getSeatNumber())
                            .status(seat.getStatus())
                            .build());
                }
            }
        }
        return seatDTOs;
    }
}
