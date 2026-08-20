package com.booking_ticket_platform.booking.service;

import com.booking_ticket_platform.booking.dto.BookingRequest;
import com.booking_ticket_platform.booking.repository.IBookingRepository;
import com.booking_ticket_platform.concert.entity.Concert;
import com.booking_ticket_platform.concert.entity.Seat;
import com.booking_ticket_platform.concert.entity.TicketCategory;
import com.booking_ticket_platform.concert.repository.IConcertRepository;
import com.booking_ticket_platform.concert.repository.ISeatRepository;
import com.booking_ticket_platform.concert.repository.ITicketCategoryRepository;
import com.booking_ticket_platform.auth.entity.User;
import com.booking_ticket_platform.auth.repository.UserRepository;
import com.booking_ticket_platform.payment.entity.Voucher;
import com.booking_ticket_platform.payment.repository.IVoucherRepository;
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

@SpringBootTest
@ActiveProfiles("test")
public class VoucherConcurrencyTest {

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
    private IVoucherRepository voucherRepository;
    @Autowired
    private IPaymentRepository paymentRepository;

    private List<UUID> userIds = new ArrayList<>();
    private UUID concertId;
    private List<UUID> seatIds = new ArrayList<>();
    private String voucherCode = "TEST-1-USAGE";

    @BeforeEach
    public void setup() {
        seatRepository.findAll().forEach(s -> { s.setBooking(null); seatRepository.save(s); });
        paymentRepository.deleteAll();
        bookingRepository.deleteAll();
        seatRepository.deleteAll();
        ticketCategoryRepository.deleteAll();
        concertRepository.deleteAll();
        venueRepository.deleteAll();
        userRepository.deleteAll();
        voucherRepository.deleteAll();

        // Create 10 users
        for (int i = 0; i < 10; i++) {
            User user = new User();
            user.setEmail("vuser" + i + "@test.com");
            user.setPasswordHash("hash");
            user.setRole("CUSTOMER");
            user = userRepository.save(user);
            userIds.add(user.getId());
        }

        Venue venue = new Venue();
        venue.setName("Voucher Venue");
        venue.setAddress("123 Test");
        venue.setCapacity(100);
        venue.setCity("HCM");
        venue = venueRepository.save(venue);

        Concert concert = new Concert();
        concert.setName("Voucher Concert");
        concert.setVenue(venue);
        concert.setEventDate(OffsetDateTime.now().plusDays(10));
        concert.setStatus("PUBLISHED");
        concert.setStageLayout("FRONT");
        concert = concertRepository.save(concert);
        concertId = concert.getId();

        TicketCategory category = new TicketCategory();
        category.setConcert(concert);
        category.setName("VIP");
        category.setType("SEATED");
        category.setPrice(new BigDecimal("100000"));
        category.setTotalQuantity(10);
        category.setAvailableQuantity(10);
        category = ticketCategoryRepository.save(category);

        for (int i = 1; i <= 10; i++) {
            Seat seat = new Seat();
            seat.setTicketCategory(category);
            seat.setSeatNumber("B" + i);
            seat.setStatus("AVAILABLE");
            seat = seatRepository.save(seat);
            seatIds.add(seat.getId());
        }

        Voucher voucher = new Voucher();
        voucher.setCode(voucherCode);
        voucher.setDiscountType("FIXED_AMOUNT");
        voucher.setDiscountValue(new BigDecimal("50000"));
        voucher.setMaxUsage(1);
        voucher.setCurrentUsage(0);
        voucherRepository.save(voucher);
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
        voucherRepository.deleteAll();
    }

    @Test
    public void testConcurrentVoucherUsage() throws InterruptedException {
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
                    latch.await();
                    BookingRequest request = new BookingRequest();
                    request.setConcertId(concertId);
                    request.setIdempotencyKey(idempotencyBase + "-" + index);
                    // Each user books a different seat to avoid Seat Pessimistic Lock failing them
                    request.setSeatIds(Collections.singletonList(seatIds.get(index)));
                    request.setVoucherCode(voucherCode);

                    bookingService.createBooking(userIds.get(index), request);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        latch.countDown();
        doneLatch.await(15, TimeUnit.SECONDS);
        executorService.shutdown();

        assertEquals(1, successCount.get(), "Only 1 booking should succeed due to voucher lock");
        assertEquals(9, failCount.get(), "9 bookings should fail because voucher has max_usage = 1");

        Voucher updatedVoucher = voucherRepository.findAll().get(0);
        assertEquals(1, updatedVoucher.getCurrentUsage());
    }
}
