package com.booking_ticket_platform.payment.service;

import com.booking_ticket_platform.booking.entity.Booking;
import com.booking_ticket_platform.booking.repository.IBookingRepository;
import com.booking_ticket_platform.concert.entity.Seat;
import com.booking_ticket_platform.concert.repository.ISeatRepository;
import com.booking_ticket_platform.payment.entity.Payment;
import com.booking_ticket_platform.payment.repository.IPaymentRepository;
import com.booking_ticket_platform.shared.exception.ResourceNotFoundException;
import com.booking_ticket_platform.notification.service.INotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private final IPaymentRepository paymentRepository;
    private final IBookingRepository bookingRepository;
    private final ISeatRepository seatRepository;
    private final INotificationService notificationService;

    public PaymentService(IPaymentRepository paymentRepository,
                          IBookingRepository bookingRepository,
                          ISeatRepository seatRepository,
                          INotificationService notificationService) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.seatRepository = seatRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public Payment initiatePayment(UUID bookingId, String method) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!"PENDING".equals(booking.getStatus())) {
            throw new IllegalStateException("Booking is not pending");
        }

        Payment payment = Payment.builder()
                .booking(booking)
                .method(method)
                .status("PENDING")
                .build();

        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment completePayment(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (!"PENDING".equals(payment.getStatus())) {
            throw new IllegalStateException("Payment is not pending");
        }

        // 1. Complete the payment
        payment.setStatus("COMPLETED");

        // 2. Complete the booking
        Booking booking = payment.getBooking();
        booking.setStatus("COMPLETED");

        // 3. Mark held seats as booked
        List<Seat> seats = seatRepository.findByBookingId(booking.getId());
        for (Seat seat : seats) {
            seat.setStatus("BOOKED");
        }
        seatRepository.saveAll(seats);

        bookingRepository.save(booking);
        Payment savedPayment = paymentRepository.save(payment);
        
        // 4. Send email notification asynchronously (in a real app, use @Async or Message Queue)
        notificationService.sendPaymentSuccessNotification(booking);
        
        return savedPayment;
    }
}
