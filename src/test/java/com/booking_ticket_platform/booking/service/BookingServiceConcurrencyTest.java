package com.booking_ticket_platform.booking.service;

import com.booking_ticket_platform.booking.dto.BookingRequest;
import com.booking_ticket_platform.booking.dto.BookingDTO;
import com.booking_ticket_platform.booking.entity.Booking;
import com.booking_ticket_platform.booking.repository.IBookingRepository;
import com.booking_ticket_platform.concert.entity.Concert;
import com.booking_ticket_platform.concert.entity.Seat;
import com.booking_ticket_platform.concert.entity.TicketCategory;
import com.booking_ticket_platform.concert.repository.IConcertRepository;
import com.booking_ticket_platform.concert.repository.ISeatRepository;
import com.booking_ticket_platform.concert.repository.ITicketCategoryRepository;
import com.booking_ticket_platform.auth.entity.User;
import com.booking_ticket_platform.auth.repository.UserRepository;
import com.booking_ticket_platform.venue.entity.Venue;
import com.booking_ticket_platform.venue.repository.IVenueRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import com.booking_ticket_platform.payment.repository.IPaymentRepository;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
public class BookingServiceConcurrencyTest {

    @Autowired
    private IBookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IConcertRepository concertRepository;

    @Autowired
    private IVenueRepository venueRepository;

    @Autowired
    private ITicketCategoryRepository ticketCategoryRepository;

    @Autowired
    private ISeatRepository seatRepository;

    @Autowired
    private IBookingRepository bookingRepository;
    @Autowired
    private IPaymentRepository paymentRepository;

    private List<UUID> userIds = new ArrayList<>();
    private UUID concertId;
    private UUID ticketCategoryId;
    private UUID seatId;

    @BeforeEach
    public void setup() {
        // Clean up
        seatRepository.findAll().forEach(s -> { s.setBooking(null); seatRepository.save(s); });
        paymentRepository.deleteAll();
        bookingRepository.deleteAll();
        seatRepository.deleteAll();
        ticketCategoryRepository.deleteAll();
        concertRepository.deleteAll();
        venueRepository.deleteAll();
        userRepository.deleteAll();

        // Create 10 users to simulate concurrent requests
        for (int i = 0; i < 10; i++) {
            User user = new User();
            user.setEmail("user" + i + "@test.com");
            user.setPasswordHash("hash");
            user.setRole("CUSTOMER");
            user = userRepository.save(user);
            userIds.add(user.getId());
        }

        // Create Venue
        Venue venue = new Venue();
        venue.setName("Test Venue");
        venue.setAddress("123 Test St");
        venue.setCapacity(100);
        venue.setCity("HCM");
        venue = venueRepository.save(venue);

        // Create Concert
        Concert concert = new Concert();
        concert.setName("Test Concert");
        concert.setVenue(venue);
        concert.setEventDate(OffsetDateTime.now().plusDays(10));
        concert.setStatus("PUBLISHED");
        concert.setStageLayout("FRONT");
        concert = concertRepository.save(concert);
        concertId = concert.getId();

        // Create Ticket Category
        TicketCategory category = new TicketCategory();
        category.setConcert(concert);
        category.setName("VIP");
        category.setType("SEATED");
        category.setPrice(new BigDecimal("1000000"));
        category.setTotalQuantity(1);
        category.setAvailableQuantity(1);
        category = ticketCategoryRepository.save(category);
        ticketCategoryId = category.getId();

        // Create 1 Seat
        Seat seat = new Seat();
        seat.setTicketCategory(category);
        seat.setSeatNumber("A1");
        seat.setStatus("AVAILABLE");
        seat = seatRepository.save(seat);
        seatId = seat.getId();
    }

    @AfterEach
    public void cleanup() {
        seatRepository.findAll().forEach(s -> { s.setBooking(null); seatRepository.save(s); });
        paymentRepository.deleteAll();
        bookingRepository.deleteAll();
        seatRepository.deleteAll();
        ticketCategoryRepository.deleteAll();
        concertRepository.deleteAll();
        venueRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void testConcurrentSeatBooking_OversellProtection() throws InterruptedException {
        int numberOfThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        String idempotencyBase = UUID.randomUUID().toString();

        for (int i = 0; i < numberOfThreads; i++) {
            final int index = i;
            executorService.execute(() -> {
                try {
                    latch.await(); // wait for all threads to be ready
                    BookingRequest request = new BookingRequest();
                    request.setConcertId(concertId);
                    request.setIdempotencyKey(idempotencyBase + "-" + index);
                    request.setSeatIds(Collections.singletonList(seatId));

                    bookingService.createBooking(userIds.get(index), request);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        latch.countDown(); // start all threads simultaneously
        doneLatch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        // Verify only 1 booking succeeded
        assertEquals(1, successCount.get(), "Only 1 booking should succeed due to pessimistic lock");
        assertEquals(9, failCount.get(), "9 bookings should fail");

        // Verify Database State
        List<Booking> bookings = bookingRepository.findAll();
        assertEquals(1, bookings.size());
        assertEquals("PENDING", bookings.get(0).getStatus());

        Seat updatedSeat = seatRepository.findById(seatId).orElseThrow();
        assertEquals("HELD", updatedSeat.getStatus());
        assertEquals(bookings.get(0).getId(), updatedSeat.getBooking().getId());
    }

    @Test
    public void testIdempotency() {
        BookingRequest request = new BookingRequest();
        request.setConcertId(concertId);
        String idempotencyKey = UUID.randomUUID().toString();
        request.setIdempotencyKey(idempotencyKey);
        request.setSeatIds(Collections.singletonList(seatId));

        // First call
        BookingDTO booking1 = bookingService.createBooking(userIds.get(0), request);

        // Second call with same idempotency key should throw Exception
        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () -> {
            bookingService.createBooking(userIds.get(1), request);
        });

        assertNotNull(booking1);
        List<Booking> totalBookings = bookingRepository.findAll();
        assertEquals(1, totalBookings.size(), "Database should only have 1 booking for the same idempotency key");
    }
}
