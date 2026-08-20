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
    TicketCategoryDTO addTicketCategory(UUID concertId, TicketCategoryCreateRequest request);
    List<ConcertDTO> getPublishedConcerts();
    ConcertDTO getConcertById(UUID id);
}
