package com.booking_ticket_platform.booking.scheduler;

import com.booking_ticket_platform.booking.service.BookingService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class BookingCancellationScheduler {

    private final BookingService bookingService;

    public BookingCancellationScheduler(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // Run every minute (60,000 ms)
    @Scheduled(fixedRate = 60000)
    public void cancelExpiredPendingBookings() {
        // Cutoff time: 5 minutes ago
        OffsetDateTime cutoffTime = OffsetDateTime.now().minusMinutes(5);
        bookingService.cancelExpiredBookings(cutoffTime);
    }
}
