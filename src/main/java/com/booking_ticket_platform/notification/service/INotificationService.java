package com.booking_ticket_platform.notification.service;

import com.booking_ticket_platform.booking.entity.Booking;

public interface INotificationService {
    void sendPaymentSuccessNotification(Booking booking);
}
