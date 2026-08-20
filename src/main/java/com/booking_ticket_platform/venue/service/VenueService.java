package com.booking_ticket_platform.venue.service;

import com.booking_ticket_platform.venue.dto.VenueCreateRequest;
import com.booking_ticket_platform.venue.dto.VenueDTO;
import com.booking_ticket_platform.venue.entity.Venue;
import com.booking_ticket_platform.venue.repository.IVenueRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VenueService implements IVenueService {

    private final IVenueRepository venueRepository;

    public VenueService(IVenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    @Override
    public VenueDTO createVenue(VenueCreateRequest request) {
        Venue venue = Venue.builder()
                .name(request.getName())
                .address(request.getAddress())
                .city(request.getCity())
                .capacity(request.getCapacity())
                .build();
        Venue saved = venueRepository.save(venue);
        return mapToDTO(saved);
    }

    @Override
    public List<VenueDTO> getAllVenues() {
        return venueRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteVenue(java.util.UUID id) {
        if (!venueRepository.existsById(id)) {
            throw new com.booking_ticket_platform.shared.exception.ResourceNotFoundException("Venue not found");
        }
        try {
            venueRepository.deleteById(id);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            throw new IllegalStateException("Cannot delete venue because it is used by existing concerts.");
        }
    }

    private VenueDTO mapToDTO(Venue venue) {
        return VenueDTO.builder()
                .id(venue.getId())
                .name(venue.getName())
                .address(venue.getAddress())
                .city(venue.getCity())
                .capacity(venue.getCapacity())
                .build();
    }
}
