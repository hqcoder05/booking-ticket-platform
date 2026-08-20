package com.booking_ticket_platform.concert.service;

import com.booking_ticket_platform.concert.dto.ConcertCreateRequest;
import com.booking_ticket_platform.concert.dto.ConcertDTO;
import com.booking_ticket_platform.concert.dto.TicketCategoryCreateRequest;
import com.booking_ticket_platform.concert.dto.TicketCategoryDTO;

import java.util.List;
import java.util.UUID;

public interface IConcertService {
    ConcertDTO createConcert(ConcertCreateRequest request);
    ConcertDTO updateConcert(UUID id, ConcertCreateRequest request);
    ConcertDTO publishConcert(UUID id);
    ConcertDTO cancelConcert(UUID id);
    void deleteConcert(UUID id);
    TicketCategoryDTO addTicketCategory(UUID concertId, TicketCategoryCreateRequest request);
    TicketCategoryDTO updateTicketCategory(UUID concertId, UUID categoryId, TicketCategoryCreateRequest request);
    void deleteTicketCategory(UUID concertId, UUID categoryId);
    List<ConcertDTO> getPublishedConcerts();
    List<ConcertDTO> getAllConcerts();
    ConcertDTO getConcertById(UUID id);
    List<com.booking_ticket_platform.concert.dto.SeatDTO> getConcertSeats(UUID concertId);
}
